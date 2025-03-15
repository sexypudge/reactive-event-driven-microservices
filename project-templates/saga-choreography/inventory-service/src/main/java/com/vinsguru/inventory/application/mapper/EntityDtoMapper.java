package com.vinsguru.inventory.application.mapper;

import com.vinsguru.inventory.application.entity.OrderInventory;
import com.vinsguru.inventory.common.dto.InventoryDto;
import com.vinsguru.inventory.common.dto.InventoryProcessRequest;

public class EntityDtoMapper {
    public static OrderInventory toInventory(InventoryProcessRequest request) {
        return OrderInventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .orderId(request.orderId())
                .build();
    }

    public static InventoryDto toInventoryDto(OrderInventory inventory) {
        return InventoryDto.builder()
                .inventoryId(inventory.getInventoryId())
                .orderId(inventory.getOrderId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .status(inventory.getStatus())
                .build();
    }
}
