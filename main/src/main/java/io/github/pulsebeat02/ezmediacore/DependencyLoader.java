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
import io.github.pulsebeat02.ezmediacore.dependency.LibraryDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.SimpleRTSPServerDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.VLCDependencyManager;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    this.installDependencies();
    this.downloadNativeLibraries();
    this.shutdown();
  }

  private void downloadNativeLibraries() {
    try {
      CompletableFuture.allOf(
              CompletableFuture.runAsync(this::installVLC, this.service),
              CompletableFuture.runAsync(this::installFFmpeg, this.service),
              CompletableFuture.runAsync(this::installRTSP, this.service),
              CompletableFuture.runAsync(this::installNativeLibraries, this.service))
          .handle(Throwing.THROWING_FUTURE)
          .get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new AssertionError(e);
    }
  }

  private void shutdown() {
    this.service.shutdown();
  }

  private void installNativeLibraries() {
    try {
      new DitherDependencyManager(this.core).start();
      this.core.getLogger().info(Locale.FINISHED_DEPENDENCY_LOAD.build("Dither"));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void installFFmpeg() {
    try {
      new FFmpegDependencyManager(this.core).start();
      this.core.getLogger().info(Locale.FINISHED_DEPENDENCY_LOAD.build("FFmpeg"));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void installDependencies() {
    try {
      new LibraryDependencyManager(this.core).start();
    } catch (final IOException | ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private void installVLC() {
    try {
      new VLCDependencyManager(this.core).start();
      this.loadNativeLibVLC();
      this.core.getLogger().info(Locale.FINISHED_DEPENDENCY_LOAD.build("VLC"));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void loadNativeLibVLC() {
    if (this.isSupported()) {
      this.executePhantomPlayers();
    }
  }

  private boolean isSupported() {
    return this.core.isVLCSupported();
  }

  private void executePhantomPlayers() {
    new NativePluginLoader(this.core).executePhantomPlayers();
  }

  private void installRTSP() {
    try {
      new SimpleRTSPServerDependencyManager(this.core).start();
      this.core.getLogger().info(Locale.FINISHED_DEPENDENCY_LOAD.build("RTSP"));
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
