package com.vinsguru.order.messaging.config;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.Response;
import com.vinsguru.common.messages.inventory.InventoryRequest;
import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.shipping.ShippingRequest;
import com.vinsguru.common.publisher.EventPublisher;
import com.vinsguru.common.util.MessageConverter;
import com.vinsguru.order.messaging.orchestrator.OrderFulfillmentOrchestrator;
import com.vinsguru.order.messaging.orchestrator.impl.Workflow;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class OrderFulfillmentOrchestratorConfig {
    private static final String DESTINATION_HEADER = "spring.cloud.stream.sendto.destination";
    private static final String PAYMENT_REQUEST_CHANNEL = "payment-request-channel";
    private static final String INVENTORY_REQUEST_CHANNEL = "inventory-request-channel";
    private static final String SHIPPING_REQUEST_CHANNEL = "shipping-request-channel";

    private final OrderFulfillmentOrchestrator orchestrator;

    private final EventPublisher<UUID> eventPublisher;
    private Workflow workflow;

//    @Bean
//    public Supplier<Flux<Message<Request>>> orderEventProducer() {
//        return () -> eventPublisher.publish()
//                .flatMap(uuid -> workflow.getFirstStep().send(uuid))
//                .map(this::toMessage);
//    }

    @Bean
    public Function<Flux<Message<Response>>, Flux<Message<Request>>> orderOrchestrator() {
        return flux -> flux.map(MessageConverter::toRecord)
                .doOnNext(r -> log.info("[orderOrchestrator] order service received {}", r.message()))
                .concatMap(r -> Flux.from(orchestrator.orchestrate(r.message()))
                        .doOnNext(i -> log.info("[orderOrchestrator] orchestrator.orchestrate emitting: {}", i))
                        .doAfterTerminate(() -> r.acknowledgement().acknowledge())
                )
                .mergeWith(orchestrator.orderInitialRequests()) // merge với các đơn hàng mới phát ra từ eventPublisher emitted by OrderController
                .map(this::toMessage);
    }

    public Message<Request> toMessage(Request request) {
        log.info("[toMessage] order service produced request: {}", request);
        return MessageBuilder.withPayload(request)
                .setHeader(KafkaHeaders.KEY, request.orderId().toString())
                .setHeader(DESTINATION_HEADER, this.getDestinationHeader(request))
                .build();
    }


    private String getDestinationHeader(Request request) {
        return switch (request) {
            case PaymentRequest ignored -> PAYMENT_REQUEST_CHANNEL;
            case InventoryRequest ignored -> INVENTORY_REQUEST_CHANNEL;
            case ShippingRequest ignored -> SHIPPING_REQUEST_CHANNEL;
            default -> throw new IllegalStateException("Unexpected value: " + request);
        };
    }
}
