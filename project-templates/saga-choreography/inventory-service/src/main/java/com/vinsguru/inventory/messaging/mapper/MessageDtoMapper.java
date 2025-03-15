package com.vinsguru.inventory.messaging.mapper;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.inventory.common.dto.InventoryDto;
import com.vinsguru.inventory.common.dto.InventoryProcessRequest;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.function.Function;

public class MessageDtoMapper {

    public static InventoryProcessRequest toInventoryProcessRequest(OrderEvent.Created event) {
        return InventoryProcessRequest.builder()
                .orderId(event.orderId())
                .productId(event.productId())
                .quantity(event.quantity())
                .build();
    }

    public static InventoryEvent toInventoryDeductedEvent(InventoryDto dto) {
        return InventoryEvent.Deducted.builder()
                .orderId(dto.orderId())
                .productId(dto.productId())
                .quantity(dto.quantity())
                .inventoryId(dto.inventoryId())
                .createdAt(Instant.now())
                .build();
    }

    public static InventoryEvent toInventoryRestoredEvent(InventoryDto orderInventoryDto) {
        return InventoryEvent.Restored.builder()
                .orderId(orderInventoryDto.orderId())
                .inventoryId(orderInventoryDto.inventoryId())
                .productId(orderInventoryDto.productId())
                .quantity(orderInventoryDto.quantity())
                .createdAt(Instant.now())
                .build();
    }


    public static Function<Throwable, Mono<InventoryEvent>> toInventoryDeclinedEvent(OrderEvent.Created event) {
        return ex -> Mono.fromSupplier(() -> InventoryEvent.Declined.builder()
                .orderId(event.orderId())
                .productId(event.productId())
                .quantity(event.quantity())
                .createdAt(Instant.now())
                .message(ex.getMessage())
                .build());
    }

    public static Mono<InventoryEvent> toInventoryDeclinedEvent(OrderEvent.Created event, Exception ex) {
        return Mono.fromSupplier(() -> InventoryEvent.Declined.builder()
                .orderId(event.orderId())
                .productId(event.productId())
                .quantity(event.quantity())
                .createdAt(Instant.now())
                .message(ex.getMessage())
                .build());
    }
}
