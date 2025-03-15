package com.vinsguru.common.util;

import com.vinsguru.common.exception.EventAlreadyProcessedException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;
/**
 * is designed to prevent duplicate event processing in a reactive Spring WebFlux application.
 * It ensures that if an event has already been processed (e.g., exists in a database),
 * the processing flow is halted with an error
 *         isPresentInDB(someId): Returns Mono<Boolean> (true if the event is a duplicate).
 *         process(someId): Runs the actual processing if the event is not a duplicate.
 */
@Slf4j
public class DuplicateEventValidator {

    public static Function<Mono<Boolean>, Mono<Void>> emitErrorForRedundantProcessing() {
        return result -> result
                .flatMap(b -> b ? Mono.error(new EventAlreadyProcessedException("Duplicate event")) : Mono.empty())
                .doOnError(EventAlreadyProcessedException.class, e -> log.warn("Duplicate event ", e))
                .then();
    }

    public static <T> Mono<T> validate(Mono<Boolean> eventValidationPublisher, Mono<T> eventProcessingPublisher) {
        return eventValidationPublisher
                .transform(emitErrorForRedundantProcessing())
                .then(eventProcessingPublisher);
    }
    /*


     */
        /*

            DuplicateEventValidator.validate(  isPresentInDB(some-id), process(some-id) )
                                .doOnNext(...)
                                .map(...)
                                ...
                                ...

     */

}
