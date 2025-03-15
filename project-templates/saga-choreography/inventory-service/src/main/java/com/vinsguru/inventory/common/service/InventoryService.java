package com.vinsguru.inventory.common.service;

import com.vinsguru.inventory.common.dto.InventoryDto;
import com.vinsguru.inventory.common.dto.InventoryProcessRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryService {
    Mono<InventoryDto> deduct(InventoryProcessRequest request);

    Mono<InventoryDto> restore(UUID orderId);
}
