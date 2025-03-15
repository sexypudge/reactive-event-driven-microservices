package com.vinsguru.order.common.service;

import com.vinsguru.order.common.dto.PurchaseOrderDto;

/**
 * When a new order is created, this interface ensures that other parts of the system are notified
 */
public interface OrderEventListener {
    void emitOrderCreated(PurchaseOrderDto dto);
}
