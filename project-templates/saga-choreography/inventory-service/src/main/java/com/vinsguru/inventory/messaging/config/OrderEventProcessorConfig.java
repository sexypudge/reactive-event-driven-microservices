package com.vinsguru.inventory.messaging.config;

import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.common.util.MessageConverter;
import com.vinsguru.common.util.Record;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.backoff.ExponentialBackOff;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
class KafkaErrorHandlerConfig {

    private static final String DLT_TOPIC = "dlt-inventory-topic";

    /**
     * Nếu em dùng DefaultErrorHandler với DeadLetterPublishingRecoverer,
     * Kafka sẽ tự động acknowledge khi gửi vào DLT, nên em không cần gọi .acknowledge() nữa.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Cấu hình Exponential Backoff
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(2000); // Lần retry đầu tiên: 2 giây
        backOff.setMultiplier(2.0);       // Nhân đôi thời gian mỗi lần retry
        backOff.setMaxInterval(10000);    // Giới hạn tối đa 10 giây

        // Khi retry thất bại, gửi vào DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    log.error("Message {} failed after retries, sending to DLT. Error: {}", record.value(), ex.getMessage());
                    return new TopicPartition(DLT_TOPIC, record.partition());
                }
        );

        return new DefaultErrorHandler(recoverer, backOff);
    }
}


@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorConfig {

    private static final String DLT_TOPIC = "dlt-inventory-topic";

    private final OrderEventProcessor<InventoryEvent> orderEventProcessor;
    private final StreamBridge streamBridge;

    @Bean
    public Function<Flux<Message<OrderEvent>>, Flux<Message<InventoryEvent>>> processor() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> log.info("inventory service received: {}", r.message()))
                .concatMap(r -> this.orderEventProcessor.process(r.message())
                        .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))) // Mỗi lần retry cách nhau 2s, tăng dần
                        .onErrorResume(ex -> Mono.fromRunnable(() -> this.handleError(ex, r)))
                        .doOnSuccess(e -> r.acknowledgement().acknowledge())
                )
                .map(OrderSaga::toMessage);

    }

    private void handleError(Throwable ex, Record<OrderEvent> record) {
        log.error("Error processing OrderEvent: key={}, message={}, error={}",
                record.key(), record.message(), ex.getMessage(), ex);

        Message<OrderEvent> dltMessage = MessageBuilder.withPayload(record.message())
                .setHeader(KafkaHeaders.KEY, record.key())
                .setHeader("error", ex.getMessage()) // Lưu thông tin lỗi
                .setHeader("timestamp", System.currentTimeMillis()) // Thời gian lỗi xảy ra
                .setHeader("retry-count", 2) // Số lần retry trước khi vào DLT
                .setHeader("source-service", "inventory-service") // Service gây lỗi
                .build();

        // Gửi vào DLT trước, rồi mới acknowledge → Đảm bảo message không bị mất.
        this.streamBridge.send(DLT_TOPIC, dltMessage);

        // Acknowledge message để tránh re-delivery vô hạn
        record.acknowledgement().acknowledge();
    }


    private Message<InventoryEvent> toMessage(InventoryEvent event) {
        return MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, event.orderId().toString())
                .build();
    }

    /**
     * Reprocess DLT messages
     * 1. Get DLT messages from DLT topic
     * 2. Process DLT messages
     * 3. Acknowledge DLT messages
     * 4. Handle error
     * 5. Retry
     * 6. Send to DLT topic
     * TODO: need to register this consumer dltInventoryConsumer in the application.yml
     */

    @Bean
    public Consumer<Flux<Message<OrderEvent>>> dltInventoryConsumer() {
        return flux -> flux
                .doOnNext(msg -> log.warn("Processing DLT message: {}", msg.getPayload()))
                .flatMap(this::reprocessMessage)
                .onErrorContinue((ex, msg) -> log.error("Failed to reprocess DLT message: {}", msg, ex));
    }

    private Mono<Void> reprocessMessage(Message<OrderEvent> msg) {
        return this.orderEventProcessor.process(msg.getPayload())
                .doOnSuccess(e -> log.info("Successfully reprocessed: {}", msg.getPayload()))
                .then();
    }

}
