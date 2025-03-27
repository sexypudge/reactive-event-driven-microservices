package com.vinsguru.order.messaging.orchestrator.impl;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.orchestrator.RequestCompensator;
import com.vinsguru.common.orchestrator.RequestSender;
import com.vinsguru.order.common.enums.WorkflowAction;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.WorkflowActionTracker;
import com.vinsguru.order.messaging.mapper.MessageDtoMapper;
import com.vinsguru.order.messaging.orchestrator.PaymentStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStepImpl implements PaymentStep {

    private final WorkflowActionTracker tracker;
    private final OrderFulfillmentService fulfillmentService;
    private RequestCompensator previousStep;
    private RequestSender nextStep;

    @Override
    public Publisher<Request> compensate(UUID orderId) { // keep track and prepare a rollback payment request
        return tracker.track(orderId, WorkflowAction.PAYMENT_REFUND_INITIATED)
                .thenReturn(MessageDtoMapper.toPaymentRefundRequest(orderId))
                .cast(Request.class)
                .concatWith(previousStep.compensate(orderId));
    }

    @Override
    public Publisher<Request> send(UUID orderId) { // keep track and prepare a process payment request
        return tracker.track(orderId, WorkflowAction.PAYMENT_REQUEST_INITIATED)
                .then(this.fulfillmentService.get(orderId))
                .map(MessageDtoMapper::toPaymentProcessRequest);
    }


    @Override
    public Publisher<Request> onSuccess(PaymentResponse.Processed response) {
        log.info("[PaymentStep - onSuccess]: {}", response);
        return tracker.track(response.orderId(), WorkflowAction.PAYMENT_PROCESSED)
                .then(Mono.from(nextStep.send(response.orderId()))) // in case of success -> notify the next step, so we use Mono.from
//                .thenMany(Mono.from(nextStep.send(response.orderId())))
                ;
    }

    @Override
    public Publisher<Request> onFailure(PaymentResponse.Declined response) {
        return tracker.track(response.orderId(), WorkflowAction.PAYMENT_DECLINED)
                .thenMany(previousStep.compensate(response.orderId())); // in case of failure -> notify previous steps, so we use thenMany
    }

    @Override
    public void setPreviousStep(RequestCompensator previousStep) {
        this.previousStep = previousStep;
    }

    @Override
    public void setNextStep(RequestSender nextStep) {
        this.nextStep = nextStep;
    }
}
