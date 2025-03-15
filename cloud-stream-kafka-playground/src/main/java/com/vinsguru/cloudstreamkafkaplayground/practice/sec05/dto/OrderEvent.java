package com.vinsguru.cloudstreamkafkaplayground.practice.sec05.dto;

public record OrderEvent(int customerId,
                         int productId,
                         OrderType orderType) {
}
