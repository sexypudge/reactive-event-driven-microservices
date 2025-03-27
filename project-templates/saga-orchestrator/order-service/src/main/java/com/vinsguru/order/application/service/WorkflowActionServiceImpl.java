package com.vinsguru.order.application.service;

import com.vinsguru.common.util.DuplicateEventValidator;
import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.application.repository.OrderWorkflowActionRepository;
import com.vinsguru.order.common.dto.OrderWorkflowActionDto;
import com.vinsguru.order.common.enums.WorkflowAction;
import com.vinsguru.order.common.service.WorkflowActionRetriever;
import com.vinsguru.order.common.service.WorkflowActionTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class WorkflowActionServiceImpl implements WorkflowActionTracker, WorkflowActionRetriever {
    private final OrderWorkflowActionRepository repository;

    @Override
    public Flux<OrderWorkflowActionDto> retrieve(UUID orderId) {
        return repository.findByOrderIdOrderByCreatedAt(orderId)
                .map(EntityDtoMapper::toOrderWorkflowActionDto);
    }

    @Override
    public Mono<Void> track(UUID orderId, WorkflowAction action) {
        return DuplicateEventValidator.validate(repository.existsByOrderIdAndAction(orderId, action),
                Mono.defer(() -> repository.save(EntityDtoMapper.toOrderWorkflowAction(orderId, action))) // delay
        ).then();
    }
}
