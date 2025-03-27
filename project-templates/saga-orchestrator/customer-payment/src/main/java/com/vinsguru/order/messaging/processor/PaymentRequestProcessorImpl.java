package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.order.common.service.PaymentService;
import com.vinsguru.order.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestProcessorImpl implements PaymentRequestProcessor {
   private final PaymentService paymentService;

    @Override
    public Mono<PaymentResponse> handle(PaymentRequest.Process request) {
        var dto = MessageDtoMapper.toProcessRequest(request);
        return paymentService.process(dto)
                .map(MessageDtoMapper::toProcessedResponse)
                .transform(exceptionHandler(request));
    }

    @Override
    public Mono<PaymentResponse> handle(PaymentRequest.Refund request) {
        return paymentService.refund(request.orderId())
                .then(Mono.empty());
    }

    private UnaryOperator<Mono<PaymentResponse>> exceptionHandler(PaymentRequest.Process request) {
        return mono -> mono
                .onErrorResume(EventAlreadyProcessedException.class, ex -> Mono.empty())
                .onErrorResume(MessageDtoMapper.toPaymentDeclinedResponse(request));
    }
}
