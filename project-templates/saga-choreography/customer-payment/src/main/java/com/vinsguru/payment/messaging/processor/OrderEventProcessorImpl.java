package com.vinsguru.payment.messaging.processor;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.processor.ordersaga.OrderEventProcessor;
import com.vinsguru.payment.common.service.PaymentService;
import com.vinsguru.payment.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessorImpl implements OrderEventProcessor<PaymentEvent> {
    private final PaymentService paymentService;

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.Created event) {
        return paymentService.process(MessageDtoMapper.toPaymentProcessRequest(event))
                .map(MessageDtoMapper::toPaymentDeductedEvent)
                .doOnNext(paymentDeductedEvent -> log.info("payment processed {}", paymentDeductedEvent))
                .transform(exceptionHandler(event));
    }

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.Completed event) {
        return Mono.empty();
    }

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.Cancelled event) {
        return paymentService.refund(event.orderId())
                .map(MessageDtoMapper::toPaymentRefundedEvent)
                .doOnNext(e -> log.info("refund  processed {}", e))
                .doOnError(ex -> log.error("error while processing refund", ex));
    }

    private UnaryOperator<Mono<PaymentEvent>> exceptionHandler(OrderEvent.Created event) {
        return mono -> mono
                .onErrorResume(EventAlreadyProcessedException.class, ex -> {
                    log.info("Event already processed for orderId: {}", event.orderId());
                    return Mono.empty();
                })
//                .onErrorResume(CustomerNotFoundException.class, MessageDtoMapper.toPaymentDeclinedEvent(event))
//                .onErrorResume(InSufficientBalanceException.class, MessageDtoMapper.toPaymentDeclinedEvent(event));
                .onErrorResume(MessageDtoMapper.toPaymentDeclinedEvent(event));
    }

}
