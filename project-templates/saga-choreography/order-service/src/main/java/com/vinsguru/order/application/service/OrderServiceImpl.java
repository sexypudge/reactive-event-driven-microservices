package com.vinsguru.order.application.service;

import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.application.repository.PurchaseOrderRepository;
import com.vinsguru.order.common.dto.OrderCreateRequest;
import com.vinsguru.order.common.dto.OrderDetails;
import com.vinsguru.order.common.dto.PurchaseOrderDto;
import com.vinsguru.order.common.service.OrderEventListener;
import com.vinsguru.order.common.service.OrderService;
import com.vinsguru.order.common.service.inventory.InventoryComponentFetcher;
import com.vinsguru.order.common.service.payment.PaymentComponentFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderEventListener eventListener;
    private final PaymentComponentFetcher paymentComponentFetcher;
    private final InventoryComponentFetcher inventoryComponentFetcher;

    @Override
    public Mono<PurchaseOrderDto> placeOrder(OrderCreateRequest request) {
        var entity = EntityDtoMapper.toPurchaseOrder(request);

        return purchaseOrderRepository.save(entity)
                .map(EntityDtoMapper::toPurchaseOrderDto)
                .doOnNext(eventListener::emitOrderCreated);
    }

    @Override
    public Flux<PurchaseOrderDto> getAllOrders() {
        return purchaseOrderRepository.findAll()
                .map(EntityDtoMapper::toPurchaseOrderDto);
    }

    @Override
    public Mono<OrderDetails> getOrderDetails(UUID orderId) {
        return purchaseOrderRepository.findById(orderId)
                .map(EntityDtoMapper::toPurchaseOrderDto)
                .flatMap(d -> paymentComponentFetcher.getComponent(orderId)
                        .zipWith(inventoryComponentFetcher.getComponent(orderId))
                        .map(t -> EntityDtoMapper.toOrderDetails(d, t.getT1(), t.getT2()))

                );
    }
}
