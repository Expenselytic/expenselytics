package com.expenlytics.core.exception;

public class AccountException extends RuntimeException {

    public enum Reason {
        INVALID_INPUT,
        DUPLICATE_EMAIL,
        UNAUTHORIZED,
    }

    private final Reason reason;

    public AccountException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
