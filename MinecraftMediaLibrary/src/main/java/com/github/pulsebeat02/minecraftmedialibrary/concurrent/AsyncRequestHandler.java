package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.http.AbstractRequestHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncRequestHandler {

    private final AbstractRequestHandler request;

    public AsyncRequestHandler(@NotNull final AbstractRequestHandler request) {
        this.request = request;
    }

    public CompletableFuture<Void> handleRequest() {
        return CompletableFuture.runAsync(request::handleRequest);
    }

}
