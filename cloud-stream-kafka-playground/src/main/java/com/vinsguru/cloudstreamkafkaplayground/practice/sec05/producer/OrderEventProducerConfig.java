package com.vinsguru.cloudstreamkafkaplayground.practice.sec05.producer;

import com.vinsguru.cloudstreamkafkaplayground.practice.sec05.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.practice.sec05.dto.OrderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Configuration
@Slf4j
public class OrderEventProducerConfig {

    @Bean
    public Supplier<Flux<Message<OrderEvent>>> orderEventProducer() {
        return () -> Flux.range(1, 10)
                .delayElements(java.time.Duration.ofSeconds(1))
                .map(this::toMessage)
                .doOnNext(message -> log.info("[orderEventProducer] produced {}", message));
    }

    private Message<OrderEvent> toMessage(int i) {
        var type = i % 2 == 0 ? OrderType.DIGITAL : OrderType.PHYSICAL;
        var event = new OrderEvent(i, i , type);
        return MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.KEY, "order-id-" + i)
                .build();
    }
}
