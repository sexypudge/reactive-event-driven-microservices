package com.vinsguru.common.orchestrator;

import com.vinsguru.common.messages.Response;

/**
 * this generic interface is used to define the contract for a workflow step: Payment Step or Inventory Step, ...
 * it extends the RequestSender, RequestCompensator, and ResponseProcessor interfaces
 * @param <T>
 */
public interface WorkflowStep<T extends Response>
        extends RequestSender, RequestCompensator, WorkflowChain, ResponseProcessor<T> {
}
