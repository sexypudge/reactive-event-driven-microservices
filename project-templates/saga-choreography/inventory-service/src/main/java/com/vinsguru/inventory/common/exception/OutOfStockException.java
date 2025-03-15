package com.vinsguru.inventory.common.exception;

public class OutOfStockException extends RuntimeException {
    private static final String MESSAGE = "Product is out of stock";
    public OutOfStockException() {
        super(MESSAGE);
    }
}
