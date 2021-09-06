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

import io.github.pulsebeat02.ezmediacore.dependency.ArtifactInstaller;
import io.github.pulsebeat02.ezmediacore.dependency.FFmpegInstaller;
import io.github.pulsebeat02.ezmediacore.vlc.VLCBinaryLocator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;

public class DependencyLoader implements LibraryLoader {

  private final MediaLibraryCore core;

  public DependencyLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  @Override
  public void start() throws ExecutionException, InterruptedException {
    CompletableFuture.allOf(
            CompletableFuture.runAsync(this::installFFmpeg),
            CompletableFuture.runAsync(this::installDependencies).thenRunAsync(this::checkVLC))
        .get();
  }

  private void installFFmpeg() {
    try {
      final FFmpegInstaller installer = new FFmpegInstaller(this.core);
      installer.start();
      this.core.setFFmpegPath(installer.getExecutable());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void installDependencies() {
    try {
      new ArtifactInstaller(this.core).start();
    } catch (final IOException
        | ReflectiveOperationException
        | URISyntaxException
        | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private void checkVLC() {
    try {
      this.core.setVLCStatus(
          new VLCBinaryLocator(this.core, this.core.getVlcPath()).locate().isPresent());
      if (this.core.isVLCSupported()) {
        new NativePluginLoader().executePhantomPlayers();
      }
    } catch (final IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
