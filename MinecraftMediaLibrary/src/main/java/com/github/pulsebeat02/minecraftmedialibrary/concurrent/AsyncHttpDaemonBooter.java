package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.http.AbstractHttpDaemon;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncHttpDaemonBooter {

    private final AbstractHttpDaemon daemon;

    public AsyncHttpDaemonBooter(@NotNull final AbstractHttpDaemon daemon) {
        this.daemon = daemon;
    }

    public CompletableFuture<Void> startServer() {
        return CompletableFuture.runAsync(daemon::startServer);
    }

}
