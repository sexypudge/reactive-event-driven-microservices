package com.vinsguru.common.exception;

public class EventAlreadyProcessedException extends RuntimeException {
    private static final String Message = "The Event is already processed";

    public EventAlreadyProcessedException() {
        super(Message);
    }
    public EventAlreadyProcessedException(String message) {
        super(message);
    }
    public EventAlreadyProcessedException(String message, Throwable cause) {
        super(message, cause);
    }
    public EventAlreadyProcessedException(Throwable cause) {
        super(cause);
    }
}
