package com.vinsguru.shipment.common.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ShipmentRequest(UUID orderId,
                              Integer productId,
                              Integer customerId,
                              Integer quantity) {
}
