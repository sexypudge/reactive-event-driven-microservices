package com.vinsguru.order.messaging.orchestrator.impl;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.orchestrator.RequestCompensator;
import com.vinsguru.common.orchestrator.RequestSender;
import com.vinsguru.order.common.enums.WorkflowAction;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.WorkflowActionTracker;
import com.vinsguru.order.messaging.mapper.MessageDtoMapper;
import com.vinsguru.order.messaging.orchestrator.InventoryStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class InventoryStepImpl implements InventoryStep {
    private final WorkflowActionTracker tracker;
    private final OrderFulfillmentService fulfillmentService;
    private RequestCompensator previousStep;
    private RequestSender nextStep;


    @Override
    public Publisher<Request> onSuccess(InventoryResponse.Deducted response) {
        return tracker.track(response.orderId(), WorkflowAction.INVENTORY_DEDUCTED)
                .then(Mono.from(nextStep.send(response.orderId()))) // in case of success -> notify the next step, so we use Mono.from
//                .thenMany(Mono.from(nextStep.send(response.orderId())))
                ;
    }

    @Override
    public Publisher<Request> onFailure(InventoryResponse.Declined response) {
        return tracker.track(response.orderId(), WorkflowAction.INVENTORY_DECLINED)
                .thenMany(previousStep.compensate(response.orderId())); // in case of failure -> notify previous steps, so we use thenMany
    }

    @Override
    public Publisher<Request> compensate(UUID uuid) {
        return tracker.track(uuid, WorkflowAction.INVENTORY_RESTORE_INITIATED)
                .thenReturn(MessageDtoMapper.toInventoryRestoreRequest(uuid))
                .cast(Request.class)
                .concatWith(previousStep.compensate(uuid));
    }

    @Override
    public Publisher<Request> send(UUID uuid) {
        log.info("InventoryStepImpl - Sending request of orderId: {}", uuid);
        return tracker.track(uuid, WorkflowAction.INVENTORY_REQUEST_INITIATED)
                .then(this.fulfillmentService.get(uuid))
                .map(MessageDtoMapper::toInventoryDeductRequest);
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
