package com.vinsguru.order.application.service;

import com.vinsguru.order.application.entity.OrderPayment;
import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.application.repository.OrderPaymentRepository;
import com.vinsguru.order.common.dto.OrderPaymentDto;
import com.vinsguru.order.common.service.payment.PaymentComponentFetcher;
import com.vinsguru.order.common.service.payment.PaymentComponentStatusListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentComponentServiceImpl implements PaymentComponentFetcher, PaymentComponentStatusListener {

    private static final OrderPaymentDto DEFAULT = OrderPaymentDto.builder().build();

    private final OrderPaymentRepository orderPaymentRepository;

    @Override
    public Mono<OrderPaymentDto> getComponent(UUID orderId) {
        return orderPaymentRepository.findByOrderId(orderId)
                .map(EntityDtoMapper::toOrderPaymentDto)
                .defaultIfEmpty(DEFAULT);
    }

    @Override
    public Mono<Void> onSuccess(OrderPaymentDto message) {
        return orderPaymentRepository.findByOrderId(message.orderId())
                .switchIfEmpty(Mono.defer(() -> this.addOrderPayment(message, true)))
                .then();
    }

    @Override
    public Mono<Void> onFailure(OrderPaymentDto message) {
        return orderPaymentRepository.findByOrderId(message.orderId())
                .switchIfEmpty(Mono.defer(() -> this.addOrderPayment(message, false)))
                .then();
    }

    @Override
    public Mono<Void> onRollback(OrderPaymentDto message) {
        return orderPaymentRepository.findByOrderId(message.orderId())
                .doOnNext(s -> s.setStatus(message.status()))
                .flatMap(this.orderPaymentRepository::save)
                .then();
    }

    private Mono<OrderPayment> addOrderPayment(OrderPaymentDto dto, boolean isSuccess) {
        var entity = EntityDtoMapper.toOrderPayment(dto);
        entity.setSuccess(isSuccess);
        log.info("order payment entity: {}", entity);
        return this.orderPaymentRepository.save(entity);
    }
}
