/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.logging.Logger;
import io.github.pulsebeat02.ezmediacore.utility.throwing.ThrowingRunnable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Contract;

public class DependencyLoader {

  private final EzMediaCore core;
  private final ExecutorService service;

  DependencyLoader( final EzMediaCore core) {
    this.core = core;
    this.service = Executors.newFixedThreadPool(4);
  }

  public void start() {
    this.installNecessaryLibraries();
    this.downloadNativeLibraries();
    this.shutdown();
  }

  private void downloadNativeLibraries() {
    final CompletableFuture<?>[] futures = this.getDependencyTasks();
    try {
      CompletableFuture.allOf(futures).get();
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
      throw new AssertionError(e);
    }
  }

  @Contract(" -> new")
  private CompletableFuture<?>  [] getDependencyTasks() {
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
       final LibraryDependency dependency,  final String name) throws IOException {
    dependency.start();
    final Logger logger = this.core.getLogger();
    logger.info(Locale.DEPENDENCY_LOAD.build(name));
  }

  public  EzMediaCore getCore() {
    return this.core;
  }
}
