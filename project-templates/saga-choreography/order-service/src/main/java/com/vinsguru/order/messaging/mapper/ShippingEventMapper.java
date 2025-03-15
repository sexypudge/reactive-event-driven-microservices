package com.vinsguru.order.messaging.mapper;


import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.order.common.dto.OrderShipmentSchedule;

public class ShippingEventMapper {

    public static OrderShipmentSchedule toDto(ShippingEvent.Scheduled event) {
        return OrderShipmentSchedule.builder()
                                    .orderId(event.orderId())
                                    .deliveryDate(event.expectedDelivery())
                                    .build();
    }

}
