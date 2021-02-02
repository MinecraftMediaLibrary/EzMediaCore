package com.github.pulsebeat02.minecraftmedialibrary.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidYoutubeURLException extends AssertionError {

    private static final long serialVersionUID = -6428433369003844013L;

    public InvalidYoutubeURLException(@NotNull final String message) {
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
