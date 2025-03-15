package com.vinsguru.common.processor.ordersaga;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.processor.EventProcessor;
import reactor.core.publisher.Mono;

/**
 * R should be another DomainEvent
 * This interface is used to process OrderEvent and return another DomainEvent
 *
 * @param <R>
 */
public interface OrderEventProcessor<R extends OrderSaga> extends EventProcessor<OrderEvent, R> {

    @Override
    default Mono<R> process(OrderEvent event) {
        return switch (event) {
            case OrderEvent.Created created -> this.handle(created);
            case OrderEvent.Completed completed -> this.handle(completed);
            case OrderEvent.Cancelled cancelled -> this.handle(cancelled);
            default -> Mono.empty();
        };
    }

    Mono<R> handle(OrderEvent.Created event);

    Mono<R> handle(OrderEvent.Completed event);

    Mono<R> handle(OrderEvent.Cancelled event);
}
