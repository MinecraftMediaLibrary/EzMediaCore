package io.github.pulsebeat02.epicmedialib.ffmpeg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EnhancedExecution {

  void execute();

  void executeWithLogging(@Nullable final Consumer<String> logger);

  void executeAsync();

  @NotNull
  CompletableFuture<Void> executeAsync(@NotNull final Executor executor);

  @NotNull
  CompletableFuture<Void> executeAsyncWithLogging(@NotNull final Consumer<String> logger);

  @NotNull
  CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger, @NotNull final Executor executor);
}
