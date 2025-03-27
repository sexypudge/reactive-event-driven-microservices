package com.vinsguru.order;

import com.vinsguru.order.common.dto.OrderCreateRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class ConcurrentRequestsTest {
    public static void main(String[] args) {
        var client = WebClient.builder().baseUrl("http://localhost:8080/order").build();

        Flux.merge(
                        createRequestFlux(1, 1),
                        createRequestFlux(2, 2),
                        createRequestFlux(3, 3)
                ).flatMap(r -> client.post().bodyValue(r).retrieve().bodyToMono(Object.class).then())
                .blockLast();
    }

    private static Flux<OrderCreateRequest> createRequestFlux(int customerId, int productId) {
        var request = createRequest(customerId, productId);

        return Flux.range(1, 100)
                .map(i -> request);
    }

    private static OrderCreateRequest createRequest(int customerId, int productId) {
        return OrderCreateRequest.builder()
                .customerId(customerId)
                .productId(productId)
                .quantity(1)
                .unitPrice(1)
                .build();

    }
}
