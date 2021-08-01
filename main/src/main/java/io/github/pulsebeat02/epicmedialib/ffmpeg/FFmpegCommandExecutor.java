package io.github.pulsebeat02.epicmedialib.ffmpeg;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegCommandExecutor implements FFmpegArgumentPreparation {

  private final MediaLibraryCore core;
  private final List<String> arguments;
  private boolean completion;

  public FFmpegCommandExecutor(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.arguments = new ArrayList<>();
    this.arguments.add(core.getFFmpegPath().toString());
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addArgument(@NotNull final String arg) {
    this.arguments.add(arg);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addArguments(
      @NotNull final String key, @NotNull final String value) {
    this.arguments.add(key);
    this.arguments.add(value);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addArgument(
      @NotNull final String arg, final int index) {
    this.arguments.add(index, arg);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addArguments(
      @NotNull final String key, @NotNull final String value, final int index) {
    if (index < 0 || index > this.arguments.size() - 1) {
      return this;
    }
    this.arguments.add(index, value);
    this.arguments.add(index, key);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation removeArgument(@NotNull final String arg) {
    this.arguments.removeIf(next -> next.equals(arg));
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation removeArgument(final int index) {
    this.arguments.remove(index);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addMultipleArguments(
      @NotNull final String[] arguments) {
    Arrays.stream(arguments).forEach(this::addArgument);
    return this;
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addMultipleArguments(
      @NotNull final Collection<String> arguments) {
    arguments.forEach(this::addArgument);
    return this;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public void execute() {
    executeWithLogging(null);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    onBeforeExecution();
    final boolean consume = logger != null;
    final ProcessBuilder builder = new ProcessBuilder(this.arguments);
    builder.redirectErrorStream(true);
    try {
      final Process p = builder.start();
      try (final BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while (true) {
          line = r.readLine();
          if (line == null) {
            break;
          }
          if (consume) {
            logger.accept(line);
          }
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.completion = true;
    onAfterExecution();
  }

  @Override
  public void executeAsync() {
    CompletableFuture.runAsync(this::execute);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsync(@NotNull final Executor executor) {
    return CompletableFuture.runAsync(this::execute, executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger) {
    return CompletableFuture.runAsync(() -> executeWithLogging(logger));
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger, @NotNull final Executor executor) {
    return CompletableFuture.runAsync(() -> executeWithLogging(logger), executor);
  }

  @Override
  public void onBeforeExecution() {}

  @Override
  public void onAfterExecution() {}

  @Override
  public boolean isCompleted() {
    return this.completion;
  }

  public @NotNull List<String> getArguments() {
    return this.arguments;
  }

  @Override
  public void clearArguments() {
    this.arguments.clear();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof FFmpegCommandExecutor)) {
      return false;
    }
    final FFmpegCommandExecutor executor = (FFmpegCommandExecutor) obj;
    return this.arguments.equals(executor.getArguments()) && this.core.equals(executor.getCore());
  }
}
