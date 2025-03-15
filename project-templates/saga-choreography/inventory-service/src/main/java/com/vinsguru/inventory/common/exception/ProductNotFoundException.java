package com.vinsguru.inventory.common.exception;

public class ProductNotFoundException extends RuntimeException {
    private static final String MESSAGE = "product not found";

    public ProductNotFoundException() {
        super(MESSAGE);
    }
}
