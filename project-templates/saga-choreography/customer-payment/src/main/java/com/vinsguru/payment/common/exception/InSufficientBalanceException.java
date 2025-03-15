package com.vinsguru.payment.common.exception;

public class InSufficientBalanceException extends RuntimeException {
    private static final String MESSAGE = "Insufficient balance";

    public InSufficientBalanceException() {
        super(MESSAGE);
    }
    public InSufficientBalanceException(String message) {
        super(message);
    }
    public InSufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
    public InSufficientBalanceException(Throwable cause) {
        super(cause);
    }
}
