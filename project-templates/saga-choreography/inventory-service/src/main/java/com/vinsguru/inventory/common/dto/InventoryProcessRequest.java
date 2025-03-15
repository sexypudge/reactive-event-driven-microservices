package com.vinsguru.inventory.common.dto;

import com.vinsguru.common.events.inventory.InventoryStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record InventoryProcessRequest(UUID orderId, int quantity, int productId) {
}
