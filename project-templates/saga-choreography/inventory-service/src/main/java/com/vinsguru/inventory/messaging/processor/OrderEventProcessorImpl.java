package com.vinsguru.inventory.messaging.processor;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.inventory.common.exception.OutOfStockException;
import com.vinsguru.inventory.common.service.InventoryService;
import com.vinsguru.inventory.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorImpl implements OrderEventProcessor<InventoryEvent> {

    private final InventoryService inventoryService;

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.Created event) {
        return inventoryService.deduct(MessageDtoMapper.toInventoryProcessRequest(event))
                .map(MessageDtoMapper::toInventoryDeductedEvent)
                .doOnNext(e -> log.info("inventory deducted for {}", e.orderId()))
                .transform(exceptionHandler(event));
    }

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.Completed event) {
        return Mono.empty();
    }

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.Cancelled event) {
        return this.inventoryService.restore(event.orderId())
                .map(MessageDtoMapper::toInventoryRestoredEvent)
                .doOnNext(e -> log.info("inventory restored {}", e))
                .doOnError(ex -> log.error("error while processing restore", ex));
    }

    private UnaryOperator<Mono<InventoryEvent>> exceptionHandler(OrderEvent.Created event) {
        return mono -> mono
                .onErrorResume(EventAlreadyProcessedException.class, e -> Mono.empty())
                .onErrorResume(OutOfStockException.class, ex -> MessageDtoMapper.toInventoryDeclinedEvent(event, ex))
                .onErrorResume(MessageDtoMapper.toInventoryDeclinedEvent(event));
    }
}
