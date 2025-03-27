package com.vinsguru.order.messaging.orchestrator;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.orchestrator.WorkflowStep;
import org.reactivestreams.Publisher;

public interface PaymentStep extends WorkflowStep<PaymentResponse> {

    @Override
    default Publisher<Request> process(PaymentResponse paymentResponse) {
        return switch (paymentResponse) {
            case PaymentResponse.Processed processed -> onSuccess(processed);
            case PaymentResponse.Declined declined -> onFailure(declined);
        };
    }

    Publisher<Request> onSuccess(PaymentResponse.Processed paymentResponse);

    Publisher<Request> onFailure(PaymentResponse.Declined paymentResponse);
}
