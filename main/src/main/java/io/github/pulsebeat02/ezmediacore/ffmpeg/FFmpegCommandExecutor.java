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

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
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
  private Process process;
  private boolean completion;
  private boolean cancelled;

  public FFmpegCommandExecutor(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.arguments = new ArrayList<>();
    this.arguments.add(core.getFFmpegPath().toString());
    this.cancelled = false;
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
    this.executeWithLogging(null);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.onBeforeExecution();
    if (!this.cancelled) {
      final boolean consume = logger != null;
      final ProcessBuilder builder = new ProcessBuilder(this.arguments);
      builder.redirectErrorStream(true);
      try {
        this.process = builder.start();
        try (final BufferedReader r =
            new BufferedReader(new InputStreamReader(this.process.getInputStream()))) {
          String line;
          while (true) {
            line = r.readLine();
            if (line == null) {
              break;
            }
            if (consume) {
              logger.accept(line);
            }
            Logger.directPrintFFmpeg(line);
          }
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    this.completion = true;
    this.onAfterExecution();
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
    this.cancelled = true;
    if (this.process != null) {
      this.process.descendants().forEach(ProcessHandle::destroyForcibly);
      this.process.destroyForcibly();
      try {
        this.process.waitFor();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void onBeforeExecution() {}

  @Override
  public void onAfterExecution() {}

  @Override
  public boolean isCompleted() {
    return this.completion;
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
}
