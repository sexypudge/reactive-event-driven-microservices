package com.vinsguru.order.messaging.orchestrator;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.common.orchestrator.WorkflowStep;
import org.reactivestreams.Publisher;

public interface ShippingStep extends WorkflowStep<ShippingResponse> {

    @Override
    default Publisher<Request> process(ShippingResponse response) {
        return switch (response) {
            case ShippingResponse.Scheduled scheduled -> onSuccess(scheduled);
            case ShippingResponse.Declined declined -> onFailure(declined);
        };
    }

    Publisher<Request> onSuccess(ShippingResponse.Scheduled response);

    Publisher<Request> onFailure(ShippingResponse.Declined response);
}
