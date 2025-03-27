package com.vinsguru.order.messaging.orchestrator.impl;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.common.publisher.EventPublisher;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.messaging.orchestrator.InventoryStep;
import com.vinsguru.order.messaging.orchestrator.OrderFulfillmentOrchestrator;
import com.vinsguru.order.messaging.orchestrator.PaymentStep;
import com.vinsguru.order.messaging.orchestrator.ShippingStep;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrderFulfillmentOrchestratorImpl implements OrderFulfillmentOrchestrator {

    private final PaymentStep paymentStep;
    private final InventoryStep inventoryStep;
    private final ShippingStep shippingStep;
    private final OrderFulfillmentService fulfillmentService;
    private final EventPublisher<UUID> eventPublisher;
    private Workflow workflow;

    @PostConstruct
    private void init() {
        this.workflow = Workflow.startWith(paymentStep)
                .thenNext(inventoryStep)
                .thenNext(shippingStep)
                .doOnFailure(this::compensate)
                .doOnSuccess(this::complete);
    }

    @Override
    public Publisher<Request> orderInitialRequests() {
        return Flux.merge(
                        this.loadUnfinishedOrders(), // Load các đơn hàng cũ khi service restart (status của nó có thể là UNSUCCESSFULLY or something)
                        eventPublisher.publish() // Nhận các sự kiện order mới\
//                ,listenToKafkaOrderEvents() // Lắng nghe sự kiện từ Kafka
                ).flatMap(orderId -> workflow.getFirstStep().send(orderId))
                .doOnNext(i -> log.info("[orderInitialRequests] emitting: {}", i));
    }

    // Lắng nghe từ Kafka
//    private Flux<UUID> listenToKafkaOrderEvents() {
//        return kafkaReceiver.receive()
//                .map(record -> UUID.fromString(record.value())); // Giả sử Kafka gửi về orderId dạng String
//    }

    // Lấy danh sách đơn hàng chưa hoàn thành từ database
    private Flux<UUID> loadUnfinishedOrders() {
        return Flux.empty();
//        return fulfillmentService.getUnfinishedOrders() // Lấy danh sách đơn hàng chưa hoàn thành
//                .map(Order::getId); // Chỉ lấy UUID của đơn hàng
    }

    @Override
    public Publisher<Request> handle(PaymentResponse response) {
        return paymentStep.process(response);
    }

    @Override
    public Publisher<Request> handle(InventoryResponse response) {
        return inventoryStep.process(response);
    }

    @Override
    public Publisher<Request> handle(ShippingResponse response) {
        return shippingStep.process(response);
    }

    private Mono<Void> compensate(UUID uuid) {
        return fulfillmentService.cancel(uuid).then();
    }

    private Mono<Void> complete(UUID uuid) {
        return fulfillmentService.complete(uuid).then();
    }
}
