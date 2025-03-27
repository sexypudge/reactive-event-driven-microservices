package com.vinsguru.order.messaging.orchestrator.impl;

import lombok.Getter;
import reactor.core.publisher.Mono;

import com.vinsguru.common.orchestrator.WorkflowStep;

import java.util.UUID;
import java.util.function.Function;

public class Workflow {
    @Getter
    private final WorkflowStep<?> firstStep;
    private WorkflowStep<?> lastStep;

    private Workflow(WorkflowStep<?> firstStep) {
        this.firstStep = firstStep;
        this.lastStep = firstStep;
    }

    public static Workflow startWith(WorkflowStep<?> firstStep) {
        return new Workflow(firstStep);
    }

    public Workflow thenNext(WorkflowStep<?> nextStep) {
        this.lastStep.setNextStep(nextStep);
        nextStep.setPreviousStep(this.lastStep);
        this.lastStep = nextStep;
        return this;
    }

    public Workflow doOnSuccess(Function<UUID, Mono<Void>> function) {
        this.lastStep.setNextStep(uuid -> function.apply(uuid).then(Mono.empty()));
        return this;
    }

    public Workflow doOnFailure(Function<UUID, Mono<Void>> function) {
        this.firstStep.setPreviousStep(uuid -> function.apply(uuid).then(Mono.empty()));
        return this;
    }
}
