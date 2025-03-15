package com.vinsguru.shipment.application.service;

import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.common.events.shipping.ShippingStatus;
import com.vinsguru.common.util.DuplicateEventValidator;
import com.vinsguru.shipment.application.mapper.EntityDtoMapper;
import com.vinsguru.shipment.application.repository.ShipmentRepository;
import com.vinsguru.shipment.common.dto.ShipmentDto;
import com.vinsguru.shipment.common.dto.ShipmentRequest;
import com.vinsguru.shipment.common.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public Mono<ShipmentDto> plan(ShipmentRequest request) {
        return DuplicateEventValidator.validate(
                        this.shipmentRepository.existsByOrderId(request.orderId()),
                        Mono.defer(() -> this.planShipment(request)) // to delay planShipment method, prevents unnecessary work if the orderId already exists
                )
                .doOnNext(dto -> log.info("planned shipment for {}", dto.orderId()));
    }

    private Mono<ShipmentDto> planShipment(ShipmentRequest request) {
        var shipment = EntityDtoMapper.toShipment(request);
        shipment.setStatus(ShippingStatus.PENDING);
        return this.shipmentRepository.save(shipment)
                .map(EntityDtoMapper::toDto);
    }

    @Override
    @Transactional
    public Mono<ShipmentDto> ship(UUID orderId) {
        return this.shipmentRepository.findByOrderIdAndStatus(orderId, ShippingStatus.PENDING)
                .flatMap(t -> {
                    t.setStatus(ShippingStatus.SCHEDULED);
                    t.setDeliveryDate(Instant.now());
                    return shipmentRepository.save(t)
                            .map(EntityDtoMapper::toDto);
                })
                .doOnNext(dto -> log.info("shipment scheduled  for {}", orderId));
    }

    @Override
    @Transactional
    public Mono<Void> cancel(UUID orderId) {
        return shipmentRepository.deleteByOrderId(orderId);
    }

}
