package com.vinsguru.payment.messaging.config;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.common.util.MessageConverter;
import com.vinsguru.common.util.Record;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorConfig {
    private static final String DLT_TOPIC = "dlt-payment-topic";

    private final OrderEventProcessor<PaymentEvent> orderEventProcessor;
    private final StreamBridge streamBridge;

    @Bean
    public Function<Flux<Message<OrderEvent>>, Flux<Message<PaymentEvent>>> processor() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> log.info("payment service received: {}", r.message()))
                .concatMap(r -> this.orderEventProcessor.process(r.message())
                                .retry(2)
                                .onErrorResume(ex -> Mono.fromRunnable(() -> this.handleError(ex, r)))
//                                .doOnNext(e -> r.acknowledgement().acknowledge())
                                .doOnSuccess(e -> r.acknowledgement().acknowledge())
//                                .doAfterTerminate(() -> r.acknowledgement().acknowledge())
                )
                .map(this::toMessage);
    }

    private void handleError(Throwable ex, Record<OrderEvent> record) {
        log.error(ex.getMessage());
        this.streamBridge.send(
                DLT_TOPIC,
                MessageBuilder.withPayload(record.message())
                        .setHeader(KafkaHeaders.KEY, record.key())
                        .build()
        );
    }

    private Message<PaymentEvent> toMessage(PaymentEvent paymentEvent) {
        return MessageBuilder.withPayload(paymentEvent)
                .setHeader(KafkaHeaders.KEY, paymentEvent.orderId().toString())
                .build();
    }

}
