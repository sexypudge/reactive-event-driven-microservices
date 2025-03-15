package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.processor.ordersaga.PaymentEventProcessor;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.payment.PaymentComponentStatusListener;
import com.vinsguru.order.messaging.mapper.OrderEventMapper;
import com.vinsguru.order.messaging.mapper.PaymentEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProcessorImpl implements PaymentEventProcessor<OrderEvent> {
    private final OrderFulfillmentService orderFulfillmentService;
    private final PaymentComponentStatusListener statusListener;

    @Override
    public Mono<OrderEvent> handle(PaymentEvent.Deducted event) {
        var dto = PaymentEventMapper.toDto(event);
        return statusListener.onSuccess(dto)
                .then(orderFulfillmentService.complete(event.orderId()))
                .map(OrderEventMapper::toOrderCompletedEvent)
                .doOnNext(value -> {
                            log.info("payment emitted event: {} on thread: {}", value, Thread.currentThread().getName());
                        }
                );
    }

    @Override
    public Mono<OrderEvent> handle(PaymentEvent.Declined event) {
        var dto = PaymentEventMapper.toDto(event);
        return statusListener.onFailure(dto)
                .then(orderFulfillmentService.cancel(event.orderId()))
                .map(OrderEventMapper::toOrderCancelledEvent);
    }

    @Override
    public Mono<OrderEvent> handle(PaymentEvent.Refunded event) {
        var dto = PaymentEventMapper.toDto(event);
        return statusListener.onRollback(dto)
                .then(Mono.empty());
    }
}
