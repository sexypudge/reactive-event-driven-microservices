package com.vinsguru.order.common.service;

import com.vinsguru.order.common.dto.PaymentDto;
import com.vinsguru.order.common.dto.PaymentProcessRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentService {

    Mono<PaymentDto> process(PaymentProcessRequest request);

    Mono<PaymentDto> refund(UUID orderId);

}
