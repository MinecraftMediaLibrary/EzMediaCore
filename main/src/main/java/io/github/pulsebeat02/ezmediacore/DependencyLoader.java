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

import io.github.pulsebeat02.ezmediacore.dependency.FFmpegDependency;
import io.github.pulsebeat02.ezmediacore.dependency.LibraryDependencyManager;
import io.github.pulsebeat02.ezmediacore.dependency.SimpleRTSPServerDependency;
import io.github.pulsebeat02.ezmediacore.vlc.VLCDependency;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

public class DependencyLoader implements LibraryLoader {

  private final MediaLibraryCore core;
  private final ExecutorService executor;

  public DependencyLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void start() {
    try {
      CompletableFuture.runAsync(this::installDependencies, this.executor)
          .thenRunAsync(this::installFFmpeg, this.executor)
          .thenRunAsync(this::installVLC, this.executor)
          .thenRunAsync(this::installRTSP, this.executor)
          .get();
      this.shutdown();
    } catch (final InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void shutdown() {
    this.executor.shutdown();
  }

  private void installFFmpeg() {
    try {
      new FFmpegDependency(this.core);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void installDependencies() {
    try {
      new LibraryDependencyManager(this.core);
    } catch (final IOException
        | ReflectiveOperationException
        | URISyntaxException
        | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private void installVLC() {
    try {
      new VLCDependency(this.core);
      this.loadNativeLibVLC();
    } catch (final IOException e) {
      e.printStackTrace();
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
      new SimpleRTSPServerDependency(this.core);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
