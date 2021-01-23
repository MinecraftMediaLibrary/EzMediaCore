package com.github.pulsebeat02.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidYoutubeURLException extends AssertionError {

    public InvalidYoutubeURLException(@NotNull final String message) {
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
