package com.github.pulsebeat02.exception;

public class InvalidPackIconException extends AssertionError {

    public InvalidPackIconException(final String message) {
        super(message);
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
