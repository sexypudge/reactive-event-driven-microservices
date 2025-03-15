package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.processor.ordersaga.InventoryEventProcessor;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.inventory.InventoryComponentStatusListener;
import com.vinsguru.order.messaging.mapper.InventoryEventMapper;
import com.vinsguru.order.messaging.mapper.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProcessorImpl implements InventoryEventProcessor<OrderEvent> {
    private final OrderFulfillmentService orderFulfillmentService;
    private final InventoryComponentStatusListener statusListener;

    @Override
    public Mono<OrderEvent> handle(InventoryEvent.Deducted event) {
        var dto = InventoryEventMapper.toDto(event);

        return statusListener.onSuccess(dto)
                .then(orderFulfillmentService.complete(event.orderId()))
                .map(OrderEventMapper::toOrderCompletedEvent)
                .doOnNext(value -> {
                            log.info("inventory emitted event: {} on thread: {}", value, Thread.currentThread().getName());
                        }
                );
    }

    @Override
    public Mono<OrderEvent> handle(InventoryEvent.Restored event) {
        var dto = InventoryEventMapper.toDto(event);
        return statusListener.onRollback(dto)
                .then(Mono.empty());
    }


    @Override
    public Mono<OrderEvent> handle(InventoryEvent.Declined event) {
        var dto = InventoryEventMapper.toDto(event);
        return statusListener.onSuccess(dto)
                .then(orderFulfillmentService.cancel(event.orderId()))
                .map(OrderEventMapper::toOrderCancelledEvent);
    }
}
