package com.vinsguru.order.messaging.config;

import com.vinsguru.order.messaging.publisher.OrderEventListenerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Configuration
public class OrderEventListenerConfig {

    @Bean
    public OrderEventListenerImpl orderEventListenerAndPublisher() {
        //unicast() means only one subscriber can receive these UUIDs
        //onBackpressureBuffer() creates a queue when the consumer is slow
        var sink = Sinks.many()
                .unicast()
                .<UUID>onBackpressureBuffer();

        //Converts the sink into a Flux (stream) that can be subscribed to
        var flux = sink.asFlux();
        return new OrderEventListenerImpl(sink, flux);
    }
}
