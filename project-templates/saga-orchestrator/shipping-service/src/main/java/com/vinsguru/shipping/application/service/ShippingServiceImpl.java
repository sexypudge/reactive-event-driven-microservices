package com.vinsguru.shipping.application.service;

import com.vinsguru.common.util.DuplicateEventValidator;
import com.vinsguru.shipping.application.entity.Shipment;
import com.vinsguru.shipping.application.mapper.EntityDtoMapper;
import com.vinsguru.shipping.application.repository.ShipmentRepository;
import com.vinsguru.shipping.common.dto.ScheduleRequest;
import com.vinsguru.shipping.common.dto.ShipmentDto;
import com.vinsguru.shipping.common.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private static final Mono<Shipment> EXCEPTION = Mono.error(new IllegalArgumentException("Shipment quantity exceeded the limit"));
    private final ShipmentRepository repository;

    @Override
    public Mono<ShipmentDto> schedule(ScheduleRequest scheduleRequest) {
        return DuplicateEventValidator.validate(repository.existsByOrderId(scheduleRequest.orderId()),
                        Mono.just(scheduleRequest))
                .filter(r -> r.quantity() < 10)
                .map(EntityDtoMapper::toShipment)
                .switchIfEmpty(EXCEPTION)
                .flatMap(this::schedule);
    }

    private Mono<ShipmentDto> schedule(Shipment shipment) {
        shipment.setStatus(com.vinsguru.common.messages.shipping.ShippingStatus.SCHEDULED);
        shipment.setDeliveryDate(Instant.now().plus(Duration.ofDays(3)));
        return this.repository.save(shipment)
                .map(EntityDtoMapper::toDto);
    }
}
