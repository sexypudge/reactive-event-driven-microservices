package com.vinsguru.common.processor;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.Saga;
import reactor.core.publisher.Mono;

public interface EventProcessor<T extends DomainEvent, R extends Saga> {
    Mono<R> process(T event);
}
