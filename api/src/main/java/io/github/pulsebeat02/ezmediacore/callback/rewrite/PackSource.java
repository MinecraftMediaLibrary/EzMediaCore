package io.github.pulsebeat02.ezmediacore.callback.rewrite;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface PackSource {

  @NotNull
  CompletableFuture<Object> getFuture();
}