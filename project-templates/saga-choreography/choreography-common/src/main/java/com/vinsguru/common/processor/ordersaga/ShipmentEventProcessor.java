package com.vinsguru.common.processor.ordersaga;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.processor.EventProcessor;
import reactor.core.publisher.Mono;

public interface ShipmentEventProcessor<R extends OrderSaga> extends EventProcessor<ShippingEvent, R> {
    @Override
    default Mono<R> process(ShippingEvent event) {
        return switch (event) {
            case ShippingEvent.Scheduled scheduled -> this.handle(scheduled);
        };
    }

    Mono<R> handle(ShippingEvent.Scheduled event);

}
