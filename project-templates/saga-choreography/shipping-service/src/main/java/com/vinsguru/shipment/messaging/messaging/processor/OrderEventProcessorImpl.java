package com.vinsguru.shipment.messaging.messaging.processor;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.shipment.common.service.ShipmentService;
import com.vinsguru.shipment.messaging.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorImpl implements OrderEventProcessor<ShippingEvent> {

    private final ShipmentService service;

    @Override
    public Mono<ShippingEvent> handle(OrderEvent.Created event) {
        return this.service.plan(MessageDtoMapper.toRequest(event))
                .doOnNext(e -> log.info("shipment planned {}", e))
                .doOnError(ex -> log.error("error while processing plan", ex))
                .transform(exceptionHandler())
                .then(Mono.empty());
    }

    @Override
    public Mono<ShippingEvent> handle(OrderEvent.Completed event) {
        return this.service.ship(event.orderId())
                .map(MessageDtoMapper::toShipmentScheduledEvent)
                .doOnNext(e -> log.info("shipment scheduled {}", e))
                .doOnError(ex -> log.error("error while processing restore", ex));
    }

    @Override
    public Mono<ShippingEvent> handle(OrderEvent.Cancelled event) {
        return service.cancel(event.orderId())
                .then(Mono.empty());
    }

    private <T> UnaryOperator<Mono<T>> exceptionHandler() {
        return mono -> mono.onErrorResume(EventAlreadyProcessedException.class, ex -> Mono.empty())
                .doOnError(ex -> log.error(ex.getMessage()));
    }
}
