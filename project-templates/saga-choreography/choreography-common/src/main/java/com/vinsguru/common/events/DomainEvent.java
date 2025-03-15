package com.vinsguru.common.events;

import java.time.Instant;

public interface DomainEvent {
//    UUID orderId();
    Instant createdAt();



//    static <T extends DomainEvent> Message<T> toMessage(T event) {
//        return MessageBuilder.withPayload(event)
//                .setHeader(KafkaHeaders.KEY, event.orderId().toString())
//                .build();
//    }

}
