package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.processor.RequestProcessor;
import reactor.core.publisher.Mono;

public interface PaymentRequestProcessor extends RequestProcessor<PaymentRequest, PaymentResponse> {
    @Override
    default Mono<PaymentResponse> process(PaymentRequest request) {

        return switch (request) {
            case PaymentRequest.Refund refund -> handle(refund);
            case PaymentRequest.Process process -> handle(process);
        };
    }

    Mono<PaymentResponse> handle(PaymentRequest.Process request);

    Mono<PaymentResponse> handle(PaymentRequest.Refund request);
}
