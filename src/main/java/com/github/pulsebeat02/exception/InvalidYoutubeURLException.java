package com.github.pulsebeat02.exception;

public class InvalidYoutubeURLException extends AssertionError {

    public InvalidYoutubeURLException(final String message) {
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
