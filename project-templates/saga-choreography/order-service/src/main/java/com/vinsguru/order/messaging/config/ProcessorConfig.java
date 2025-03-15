package com.vinsguru.order.messaging.config;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.processor.EventProcessor;
import com.vinsguru.common.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

@Configuration

@Slf4j
@RequiredArgsConstructor
public class ProcessorConfig extends AbstractOrderEventRouterConfig {
    private final EventProcessor<InventoryEvent, OrderEvent> inventoryEventProcessor;
    private final EventProcessor<PaymentEvent, OrderEvent> paymentEventProcessor;
    private final EventProcessor<ShippingEvent, OrderEvent> shipmentEventProcessor;

    private final EventPublisher<OrderEvent> eventPublisher;

    @Bean
    public Function<Flux<Message<InventoryEvent>>, Flux<Message<OrderEvent>>> inventoryEventProcessor() {
        return this.processor(inventoryEventProcessor);
    }

    @Bean
    public Function<Flux<Message<PaymentEvent>>, Flux<Message<OrderEvent>>> paymentEventProcessor() {
        return this.processor(paymentEventProcessor);
    }

    @Bean
    public Function<Flux<Message<ShippingEvent>>, Flux<Message<OrderEvent>>> shipmentEventProcessor() {
        return this.processor(shipmentEventProcessor);
    }

    @Bean
    public Supplier<Flux<Message<OrderEvent>>> orderEventProducer() {
        return () -> eventPublisher.publish()
                .map(this::toMessage); // send to order-events-channel then it redirects to order-events, so other services can listen to order-events and handle message
    }
}
