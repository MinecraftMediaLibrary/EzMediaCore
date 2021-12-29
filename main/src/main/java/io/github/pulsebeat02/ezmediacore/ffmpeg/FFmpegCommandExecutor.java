/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.ffmpeg;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FFmpegCommandExecutor implements FFmpegArgumentPreparation {

  private final MediaLibraryCore core;
  private final List<String> arguments;
  private final AtomicBoolean completion;
  private final AtomicBoolean cancelled;
  private Process process;

  FFmpegCommandExecutor(@NotNull final MediaLibraryCore core) {
    checkNotNull(core, "MediaLibraryCore cannot be null!");
    this.core = core;
    this.arguments = new ArrayList<>();
    this.arguments.add(core.getFFmpegPath().toString());
    this.completion = new AtomicBoolean(false);
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract("_ -> new")
  public static @NotNull FFmpegCommandExecutor ofFFmpegExecutor(
      @NotNull final MediaLibraryCore core) {
    return new FFmpegCommandExecutor(core);
  }

  @Override
  public @NotNull FFmpegArgumentPreparation addArgument(@NotNull final String arg) {
    this.arguments.add(arg);
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
      @NotNull final String key, @NotNull final String value) {
    this.arguments.add(key);
    this.arguments.add(value);
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
    this.executeWithLogging(null);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.onBeforeExecution();
    this.completion.set(false);
    if (!this.cancelled.get()) {
      this.executeProcess(logger);
    }
    this.completion.set(true);
    this.onAfterExecution();
  }

  private void executeProcess(@Nullable final Consumer<String> logger) {
    try {
      this.process = new ProcessBuilder(this.arguments).redirectErrorStream(true).start();
      this.handleLogging(logger, logger != null);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void handleLogging(@Nullable final Consumer<String> logger, final boolean consume)
      throws IOException {
    try (final BufferedReader br = this.createFastBufferedReader()) {
      String line;
      while ((line = br.readLine()) != null) {
        this.consumeLine(consume, logger, line);
      }
    }
  }

  private void consumeLine(
      final boolean consume, @Nullable final Consumer<String> logger, @NotNull final String line) {
    if (consume) {
      logger.accept(line);
    } else {
      this.log(line);
    }
  }

  private @NotNull BufferedReader createFastBufferedReader() {
    return new BufferedReader(
        new InputStreamReader(new FastBufferedInputStream(this.process.getInputStream())));
  }

  @Override
  public void log(final String line) {
    this.core.getLogger().ffmpegPlayer(line);
  }

  @Override
  public CompletableFuture<Void> executeAsync() {
    return CompletableFuture.runAsync(this::execute);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsync(@NotNull final Executor executor) {
    return CompletableFuture.runAsync(this::execute, executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger));
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger, @NotNull final Executor executor) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger), executor);
  }

  @Override
  public void close() {
    this.cancelled.set(true);
    if (this.process != null) {
      this.process.descendants().forEach(ProcessHandle::destroyForcibly);
      this.process.destroyForcibly();
      try {
        this.process.waitFor();
      } catch (final InterruptedException e) {
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled.get();
  }

  @Override
  public void onBeforeExecution() {}

  @Override
  public void onAfterExecution() {}

  @Override
  public boolean isCompleted() {
    return this.completion.get();
  }

  @Override
  public @NotNull Process getProcess() {
    return this.process;
  }

  public @NotNull List<String> getArguments() {
    return this.arguments;
  }

  @Override
  public void clearArguments() {
    this.arguments.clear();
  }

  @Override
  public String toString() {
    return "ffmpeg %s".formatted(String.join(" ", this.arguments));
  }
}
