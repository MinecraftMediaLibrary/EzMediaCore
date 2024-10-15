package io.github.pulsebeat02.ezmediacore.pipeline.input;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface Input {
  Input EMPTY = () -> CompletableFuture.completedFuture("");
  CompletableFuture<String> getMediaRepresentation();
}
