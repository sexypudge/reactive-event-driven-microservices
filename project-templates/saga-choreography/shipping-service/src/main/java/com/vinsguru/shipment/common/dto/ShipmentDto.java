package com.vinsguru.shipment.common.dto;

import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.common.events.shipping.ShippingStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentDto(UUID shipmentId,
                                UUID orderId,
                                Integer productId,
                                Integer quantity,
                                Integer customerId,
                                Instant deliveryDate,
                                ShippingStatus status) {
}
