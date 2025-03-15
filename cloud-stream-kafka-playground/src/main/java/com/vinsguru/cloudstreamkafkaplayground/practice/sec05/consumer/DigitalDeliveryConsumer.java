package com.vinsguru.cloudstreamkafkaplayground.practice.sec05.consumer;

import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import com.vinsguru.cloudstreamkafkaplayground.practice.sec05.dto.DigitalDelivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
@Slf4j
public class DigitalDeliveryConsumer {

    @Bean
    public Function<Flux<Message<DigitalDelivery>>, Mono<Void>> digitalDeliveryFunction() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> {
                    log.info("digital delivery received: {}", r.message());
                    r.acknowledgement().acknowledge();
                })
                .then();
    }
}
