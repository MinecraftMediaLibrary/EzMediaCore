package com.github.pulsebeat02.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidPackIconException extends AssertionError {

    public InvalidPackIconException(@NotNull final String message) {
        super(message);
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }

    @Override
    public synchronized Throwable initCause(@NotNull final Throwable cause) {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
