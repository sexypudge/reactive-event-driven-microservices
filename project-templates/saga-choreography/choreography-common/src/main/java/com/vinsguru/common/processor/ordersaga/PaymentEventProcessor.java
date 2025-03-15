package com.vinsguru.common.processor.ordersaga;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.processor.EventProcessor;
import reactor.core.publisher.Mono;

public interface PaymentEventProcessor<R extends OrderSaga> extends EventProcessor<PaymentEvent, R> {

    @Override
    default Mono<R> process(PaymentEvent event) {
        return switch (event) {
            case PaymentEvent.Deducted deducted -> this.handle(deducted);
//            case PaymentEvent.Completed completed -> this.handle(completed);
            case PaymentEvent.Declined declined -> this.handle(declined);
            case PaymentEvent.Refunded refunded -> this.handle(refunded);
            default -> Mono.empty();
        };
    }

    Mono<R> handle(PaymentEvent.Deducted event);
    Mono<R> handle(PaymentEvent.Declined event);
    Mono<R> handle(PaymentEvent.Refunded event);
}
