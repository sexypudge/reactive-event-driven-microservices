package com.vinsguru.order.messaging.orchestrator;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.common.orchestrator.WorkflowStep;
import org.reactivestreams.Publisher;

public interface InventoryStep extends WorkflowStep<InventoryResponse> {

    @Override
    default Publisher<Request> process(InventoryResponse inventoryResponse) {
        return switch (inventoryResponse) {
            case InventoryResponse.Deducted deducted -> onSuccess(deducted);
            case InventoryResponse.Declined declined -> onFailure(declined);
        };
    }

    Publisher<Request> onSuccess(InventoryResponse.Deducted inventoryResponse);

    Publisher<Request> onFailure(InventoryResponse.Declined inventoryResponse);
}
