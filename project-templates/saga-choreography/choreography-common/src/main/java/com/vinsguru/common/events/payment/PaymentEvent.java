package com.vinsguru.common.events.payment;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public sealed interface PaymentEvent extends DomainEvent, OrderSaga {

    @Builder
    record Deducted(UUID orderId,
                           UUID paymentId,
                           Integer customerId,
                           Integer amount,
                           Instant createdAt) implements PaymentEvent {
    }

    @Builder
    record Refunded(UUID orderId,
                     Instant createdAt,
                     UUID paymentId,
                     Integer customerId,
                     Integer amount) implements PaymentEvent {
    }

    @Builder
    record Declined(UUID orderId,
                    Instant createdAt,
                    Integer customerId,
                    String message,
                    Integer amount) implements PaymentEvent {
    }
}
