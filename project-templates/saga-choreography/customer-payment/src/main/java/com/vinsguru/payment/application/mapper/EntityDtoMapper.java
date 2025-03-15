package com.vinsguru.payment.application.mapper;

import com.vinsguru.payment.application.entity.CustomerPayment;
import com.vinsguru.payment.common.dto.PaymentDto;
import com.vinsguru.payment.common.dto.PaymentProcessRequest;

public class EntityDtoMapper {

    public static CustomerPayment toCustomerPayment(PaymentProcessRequest request) {
        return CustomerPayment.builder()
                              .customerId(request.customerId())
                              .orderId(request.orderId())
                              .amount(request.amount())
                              .build();
    }

    public static CustomerPayment toCustomerPayment(PaymentProcessRequest request, Integer amount) {
        return CustomerPayment.builder()
                              .customerId(request.customerId())
                              .orderId(request.orderId())
                              .amount(amount)
                              .build();
    }

    public static PaymentDto toDto(CustomerPayment payment) {
        return PaymentDto.builder()
                         .paymentId(payment.getPaymentId())
                         .orderId(payment.getOrderId())
                         .customerId(payment.getCustomerId())
                         .amount(payment.getAmount())
                         .status(payment.getStatus())
                         .build();
    }
}
