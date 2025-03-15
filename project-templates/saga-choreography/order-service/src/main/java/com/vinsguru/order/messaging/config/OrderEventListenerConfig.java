package com.vinsguru.order.messaging.config;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.order.common.service.OrderEventListener;
import com.vinsguru.order.messaging.publisher.OrderEventListenerImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class OrderEventListenerConfig {

//    @Bean("orderEventListener")
    public OrderEventListener orderEventListener() {
        var sink = Sinks.many()
                .unicast()
                .<OrderEvent>onBackpressureBuffer();

        var flux = sink.asFlux();

//        return new OrderEventListenerImpl(sink, flux);
        return new OrderEventListenerImpl();
    }
}
