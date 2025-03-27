package com.vinsguru.order.messaging.config;

import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.util.MessageConverter;
import com.vinsguru.common.util.Record;
import com.vinsguru.order.messaging.processor.PaymentRequestProcessor;
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

import java.util.UUID;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorConfig {
    private static final String DLT_TOPIC = "dlt-payment-topic";
    private final PaymentRequestProcessor paymentRequestProcessor;
    private final StreamBridge streamBridge;

    @Bean
    public Function<Flux<Message<PaymentRequest>>, Flux<Message<PaymentResponse>>> processor() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> log.info("payment service received: {}, kafka_messageKey {}", r.message(), r.key()))
                .concatMap(r -> this.paymentRequestProcessor.process(r.message())
                                .retry(2)
                                .onErrorResume(ex -> Mono.fromRunnable(() -> this.handleError(ex, r)))
//                                .doOnNext(e -> r.acknowledgement().acknowledge())
                                .doOnSuccess(e -> r.acknowledgement().acknowledge())
//                                .doAfterTerminate(() -> r.acknowledgement().acknowledge())
                )
                .map(this::toMessage);
    }

    private void handleError(Throwable ex, Record<PaymentRequest> record) {
        log.error(ex.getMessage());
        this.streamBridge.send(
                DLT_TOPIC,
                MessageBuilder.withPayload(record.message())
                        .setHeader(KafkaHeaders.KEY, record.key())
                        .build()
        );
    }

    private Message<PaymentResponse> toMessage(PaymentResponse paymentResponse) {
        log.info("customer payment produced response: {}", paymentResponse);
        return MessageBuilder.withPayload(paymentResponse)
                .setHeader(KafkaHeaders.KEY, paymentResponse.orderId().toString())
//                .setHeader(KafkaHeaders.KEY, UUID.randomUUID().toString())
                .build();
    }

}
