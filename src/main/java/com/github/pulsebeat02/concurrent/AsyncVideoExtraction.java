package com.github.pulsebeat02.concurrent;

import com.github.pulsebeat02.extractor.AbstractVideoExtractor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class AsyncVideoExtraction {

    private final AbstractVideoExtractor extractor;

    AsyncVideoExtraction(@NotNull final AbstractVideoExtractor extractor) {
        this.extractor = extractor;
    }

    public CompletableFuture<File> downloadVideo() {
        return CompletableFuture.supplyAsync(extractor::downloadVideo);
    }

    public CompletableFuture<File> extractAudio() {
        return CompletableFuture.supplyAsync(extractor::extractAudio);
    }

}
