package com.vinsguru.order.messaging.mapper;


import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.order.common.dto.PurchaseOrderDto;

import java.time.Instant;

public class OrderEventMapper {

    public static OrderEvent toOrderCreatedEvent(PurchaseOrderDto dto) {
        return OrderEvent.Created.builder()
                                      .orderId(dto.orderId())
                                      .unitPrice(dto.unitPrice())
                                      .quantity(dto.quantity())
                                      .productId(dto.productId())
                                      .totalAmount(dto.amount())
                                      .customerId(dto.customerId())
                                      .createdAt(Instant.now())
                                      .build();
    }

    public static OrderEvent toOrderCancelledEvent(PurchaseOrderDto dto) {
        return OrderEvent.Cancelled.builder()
                                        .orderId(dto.orderId())
                                        .createdAt(Instant.now())
                                        .build();
    }

    public static OrderEvent toOrderCompletedEvent(PurchaseOrderDto dto) {
        return OrderEvent.Completed.builder()
                                        .orderId(dto.orderId())
                                        .createdAt(Instant.now())
                                        .build();
    }

}
