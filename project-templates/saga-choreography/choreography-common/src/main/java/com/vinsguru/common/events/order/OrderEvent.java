package com.vinsguru.common.events.order;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public sealed interface OrderEvent extends DomainEvent, OrderSaga {

    @Builder
    record Created(Instant createdAt,
                   UUID orderId,
                   Integer productId,
                   Integer customerId,
                   Integer quantity,
                   Integer unitPrice,
                   Integer totalAmount) implements OrderEvent {
    }

    @Builder
    record Completed(Instant createdAt,
                   UUID orderId) implements OrderEvent {
    }

    @Builder
    record Cancelled(Instant createdAt,
                     String message,
                     UUID orderId) implements OrderEvent {
    }
}
