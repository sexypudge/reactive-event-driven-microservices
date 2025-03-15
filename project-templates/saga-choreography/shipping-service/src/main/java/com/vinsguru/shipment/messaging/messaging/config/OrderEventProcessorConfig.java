package com.vinsguru.shipment.messaging.messaging.config;

import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.common.util.MessageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorConfig {

    private final OrderEventProcessor<ShippingEvent> eventProcessor;

    @Bean
    public Function<Flux<Message<OrderEvent>>, Flux<Message<ShippingEvent>>> processor() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> log.info("payment service received {}", r.message()))
                .concatMap(r -> this.eventProcessor.process(r.message())
                        .doOnSuccess(e -> r.acknowledgement().acknowledge())
                )
                .map(OrderSaga::toMessage);
    }

    private Message<ShippingEvent> toMessage(ShippingEvent event) {
        return MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, event.orderId().toString())
                .build();
    }

}
