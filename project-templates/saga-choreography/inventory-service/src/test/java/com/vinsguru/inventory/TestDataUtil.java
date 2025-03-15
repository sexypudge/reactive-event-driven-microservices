package com.vinsguru.inventory;

import com.vinsguru.common.events.order.OrderEvent;

import java.time.Instant;
import java.util.UUID;

public class TestDataUtil {

    public static OrderEvent.Created createOrderCreatedEvent(int customerId, int productId, int unitPrice, int quantity) {
        return OrderEvent.Created.builder()
                                      .orderId(UUID.randomUUID())
                                      .createdAt(Instant.now())
                                      .totalAmount(unitPrice * quantity)
                                      .unitPrice(unitPrice)
                                      .quantity(quantity)
                                      .customerId(customerId)
                                      .productId(productId)
                                      .build();
    }

    public static OrderEvent.Cancelled createOrderCancelledEvent(UUID orderId) {
        return OrderEvent.Cancelled.builder()
                                        .orderId(orderId)
                                        .createdAt(Instant.now())
                                        .build();
    }

}
