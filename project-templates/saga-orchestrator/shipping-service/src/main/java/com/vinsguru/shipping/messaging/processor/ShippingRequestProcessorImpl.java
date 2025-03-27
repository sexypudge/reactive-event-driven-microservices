package com.vinsguru.shipping.messaging.processor;

import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.messages.shipping.ShippingRequest;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.shipping.common.service.ShippingService;
import com.vinsguru.shipping.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@RequiredArgsConstructor
@Slf4j
@Service
public class ShippingRequestProcessorImpl implements ShippingRequestProcessor {
    private final ShippingService shippingService;

    @Override
    public Mono<ShippingResponse> handle(ShippingRequest.Schedule request) {
        var scheduleRequest = MessageDtoMapper.toScheduleRequest(request);
        return shippingService.schedule(scheduleRequest)
                .map(MessageDtoMapper::toScheduleResponse)
                .transform(exceptionHandler(request));
    }

    private UnaryOperator<Mono<ShippingResponse>> exceptionHandler(ShippingRequest.Schedule request) {
        return mono -> mono.onErrorResume(EventAlreadyProcessedException.class, e -> Mono.empty())
                .onErrorResume(MessageDtoMapper.toDeclinedShippingResponse(request));
    }
}
