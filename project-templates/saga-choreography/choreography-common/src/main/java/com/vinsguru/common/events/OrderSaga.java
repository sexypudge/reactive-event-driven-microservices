package com.vinsguru.common.events;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.UUID;

public interface OrderSaga extends Saga {
    UUID orderId();

    default <T> Message<T> toMessage(T event, Class<?> key, Integer version) {
        return MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, key.getSimpleName() + "-" + version)
                .build();
    }

    static <T extends OrderSaga> Message<T> toMessage(T event) {
        return MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, event.orderId().toString())
                .build();
    }
}
