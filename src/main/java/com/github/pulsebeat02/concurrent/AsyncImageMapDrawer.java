package com.github.pulsebeat02.concurrent;

import com.github.pulsebeat02.image.AbstractImageMapHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncImageMapDrawer {

    private final AbstractImageMapHolder imageMapHolder;

    public AsyncImageMapDrawer(@NotNull final AbstractImageMapHolder imageMapHolder) {
        this.imageMapHolder = imageMapHolder;
    }

    public CompletableFuture<Void> drawImage() {
        return CompletableFuture.runAsync(imageMapHolder::drawImage);
    }

}
