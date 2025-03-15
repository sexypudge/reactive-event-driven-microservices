package com.vinsguru.common.processor.ordersaga;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.processor.EventProcessor;
import reactor.core.publisher.Mono;

public interface InventoryEventProcessor<R extends OrderSaga> extends EventProcessor<InventoryEvent, R> {

    @Override
    default Mono<R> process(InventoryEvent event) {
        return switch (event) {
            case InventoryEvent.Deducted deducted -> this.handle(deducted);
            case InventoryEvent.Restored restored -> this.handle(restored);
            case InventoryEvent.Declined declined -> this.handle(declined);
            default -> Mono.empty();
        };
    }

    Mono<R> handle(InventoryEvent.Deducted event);

    Mono<R> handle(InventoryEvent.Restored event);

    Mono<R> handle(InventoryEvent.Declined event);

}