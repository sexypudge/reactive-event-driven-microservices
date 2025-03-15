package com.vinsguru.shipment.application.mapper;

import com.vinsguru.shipment.application.entity.Shipment;
import com.vinsguru.shipment.common.dto.ShipmentDto;
import com.vinsguru.shipment.common.dto.ShipmentRequest;

public class EntityDtoMapper {

    public static Shipment toShipment(ShipmentRequest request) {
        return Shipment.builder()
                             .orderId(request.orderId())
                             .productId(request.productId())
                             .quantity(request.quantity())
                             .customerId(request.customerId())
                             .build();
    }

    public static ShipmentDto toDto(Shipment shipment) {
        return ShipmentDto.builder()
                                .shipmentId(shipment.getId())
                                .orderId(shipment.getOrderId())
                                .productId(shipment.getProductId())
                                .quantity(shipment.getQuantity())
                                .customerId(shipment.getCustomerId())
                                .status(shipment.getStatus())
                                .deliveryDate(shipment.getDeliveryDate())
                                .build();
    }

}
