package com.github.pulsebeat02.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidPackFormatException extends AssertionError {

    public InvalidPackFormatException(@NotNull final String message) {
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
