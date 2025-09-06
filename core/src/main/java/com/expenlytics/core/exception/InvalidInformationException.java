package com.expenlytics.exception;

public class InvalidInformationException extends RuntimeException {
    public InvalidInformationException() {
        super();
    }

    public InvalidInformationException(String message) {
        super(message);
    }
}
