package com.vinsguru.shipment.common.service;

import com.vinsguru.shipment.common.dto.ShipmentDto;
import com.vinsguru.shipment.common.dto.ShipmentRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShipmentService {

    Mono<ShipmentDto> plan(ShipmentRequest request);

    Mono<ShipmentDto> ship(UUID orderId);

    Mono<Void> cancel(UUID orderId);

}
