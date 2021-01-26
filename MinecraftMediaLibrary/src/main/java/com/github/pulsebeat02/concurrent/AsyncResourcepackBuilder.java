package com.github.pulsebeat02.concurrent;

import com.github.pulsebeat02.resourcepack.AbstractPackHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncResourcepackBuilder {

    private final AbstractPackHolder packHolder;

    public AsyncResourcepackBuilder(@NotNull final AbstractPackHolder packHolder) {
        this.packHolder = packHolder;
    }

    public CompletableFuture<Void> buildResourcePack() {
        return CompletableFuture.runAsync(packHolder::buildResourcePack);
    }

}
