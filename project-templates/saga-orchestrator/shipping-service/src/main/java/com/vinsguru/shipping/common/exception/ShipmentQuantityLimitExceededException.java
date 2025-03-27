package com.vinsguru.shipping.common.exception;

public class ShipmentQuantityLimitExceededException extends RuntimeException {
    public static final String MESSAGE = "Shipment quantity limit exceeded";

    public ShipmentQuantityLimitExceededException() {
        super(MESSAGE);
    }

}
