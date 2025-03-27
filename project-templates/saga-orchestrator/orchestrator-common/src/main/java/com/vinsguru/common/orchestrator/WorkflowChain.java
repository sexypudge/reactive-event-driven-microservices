package com.vinsguru.common.orchestrator;

public interface WorkflowChain {
    void setPreviousStep(RequestCompensator previousStep); // when one step has to notify the previous step, it has to use RequestCompensator

    void setNextStep(RequestSender nextStep); // when one step has to notify the next step, it has to use RequestSender

}
