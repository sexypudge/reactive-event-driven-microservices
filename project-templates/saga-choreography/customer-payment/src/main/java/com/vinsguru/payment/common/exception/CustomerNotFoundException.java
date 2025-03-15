package com.vinsguru.payment.common.exception;

public class CustomerNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Customer not found";

    public CustomerNotFoundException() {
        super(MESSAGE);
    }
    public CustomerNotFoundException(String message) {
        super(message);
    }
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public CustomerNotFoundException(Throwable cause) {
        super(cause);
    }
}
