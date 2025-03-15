package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.processor.ordersaga.ShipmentEventProcessor;
import com.vinsguru.order.common.service.shipment.ShipmentComponentStatusListener;
import com.vinsguru.order.messaging.mapper.ShippingEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProcessorImpl implements ShipmentEventProcessor<OrderEvent> {
    private final ShipmentComponentStatusListener statusListener;

    @Override
    public Mono<OrderEvent> handle(ShippingEvent.Scheduled event) {
        var dto = ShippingEventMapper.toDto(event);
        return statusListener.onSuccess(dto)
                .then(Mono.empty());
    }
}
