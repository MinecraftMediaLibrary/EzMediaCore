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
package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.dependency.DitherDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.FFmpegDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.LibraryDependency;
import io.github.pulsebeat02.ezmediacore.dependency.LibraryDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.SimpleRTSPServerDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.VLCDependencyManager;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThrowingRunnable;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DependencyLoader implements LibraryLoader {

  private final MediaLibraryCore core;
  private final ExecutorService service;

  DependencyLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.service = Executors.newFixedThreadPool(4);
  }

  @Override
  public void start() {
    this.installNecessaryLibraries();
    this.downloadNativeLibraries();
    this.shutdown();
  }

  private void downloadNativeLibraries() {
    final CompletableFuture<?>[] futures = this.getDependencyTasks();
    try {
      CompletableFuture.allOf(futures).handle(Throwing.THROWING_FUTURE).get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new AssertionError(e);
    }
  }

  private void installNecessaryLibraries() {
    try {
      final ThrowingRunnable runnable = this::installDependencies;
      final CompletableFuture<?> dependencies = CompletableFuture.runAsync(runnable, this.service);
      dependencies.get();
    } catch (final InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Contract(" -> new")
  private CompletableFuture<?> @NotNull [] getDependencyTasks() {
    return new CompletableFuture<?>[] {
      CompletableFuture.runAsync((ThrowingRunnable) this::installVLC, this.service),
      CompletableFuture.runAsync((ThrowingRunnable) this::installFFmpeg, this.service),
      CompletableFuture.runAsync((ThrowingRunnable) this::installRTSP, this.service),
      CompletableFuture.runAsync((ThrowingRunnable) this::installNativeLibraries, this.service)
    };
  }

  private void shutdown() {
    this.service.shutdown();
  }

  private void installNativeLibraries() throws IOException {
    this.installDependency(new DitherDependencyManager(this.core), "Dither");
  }

  private void installFFmpeg() throws IOException {
    this.installDependency(new FFmpegDependencyManager(this.core), "FFmpeg");
  }

  private void installVLC() throws IOException {
    this.installDependency(new VLCDependencyManager(this.core), "VLC");
  }

  private void installRTSP() throws IOException {
    this.installDependency(new SimpleRTSPServerDependencyManager(this.core), "RTSP");
  }

  private void installDependencies() throws ReflectiveOperationException, IOException {
    this.installDependency(new LibraryDependencyManager(this.core), "Dependency Installation");
  }

  private void installDependency(
      @NotNull final LibraryDependency dependency, @NotNull final String name) throws IOException {
    dependency.start();
    final CoreLogger logger = this.core.getLogger();
    logger.info(Locale.FINISHED_DEPENDENCY_LOAD.build(name));
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
