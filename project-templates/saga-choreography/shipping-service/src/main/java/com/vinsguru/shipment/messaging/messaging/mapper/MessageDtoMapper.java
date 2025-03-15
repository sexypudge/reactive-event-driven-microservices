package com.vinsguru.shipment.messaging.messaging.mapper;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.shipment.common.dto.ShipmentDto;
import com.vinsguru.shipment.common.dto.ShipmentRequest;

import java.time.Instant;

public class MessageDtoMapper {

    public static ShipmentRequest toRequest(OrderEvent.Created event) {
        return ShipmentRequest.builder()
                                     .orderId(event.orderId())
                                     .productId(event.productId())
                                     .quantity(event.quantity())
                                     .customerId(event.customerId())
                                     .build();
    }


    public static ShippingEvent toShipmentScheduledEvent(ShipmentDto shipmentDto) {
        return ShippingEvent.Scheduled.builder()
                                               .orderId(shipmentDto.orderId())
                                               .shipmentId(shipmentDto.shipmentId())
                                               .expectedDelivery(shipmentDto.deliveryDate())
                                               .createdAt(Instant.now())
                                               .build();
    }
}
