package com.vinsguru.order.messaging.publisher;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.publisher.EventPublisher;
import com.vinsguru.order.common.dto.PurchaseOrderDto;
import com.vinsguru.order.common.service.OrderEventListener;
import com.vinsguru.order.messaging.mapper.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OrderEventListenerImpl implements OrderEventListener, EventPublisher<OrderEvent> {

//    private final Sinks.Many<OrderEvent> sink;
//    private final Flux<OrderEvent> flux;

    private final Sinks.Many<OrderEvent> sink = Sinks.many()
            .unicast()
            .onBackpressureBuffer();

    private final Flux<OrderEvent> flux = sink.asFlux();

    @Override
    public void emitOrderCreated(PurchaseOrderDto dto) {
        var event = OrderEventMapper.toOrderCreatedEvent(dto);
        this.sink.emitNext(event, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1)));
    }

    @Override
    public Flux<OrderEvent> publish() {
        return flux;
    }
}
