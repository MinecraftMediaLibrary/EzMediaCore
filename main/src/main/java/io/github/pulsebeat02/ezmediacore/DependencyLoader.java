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
import io.github.pulsebeat02.ezmediacore.dependency.VLCDependency;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class DependencyLoader implements LibraryLoader {

  private final MediaLibraryCore core;

  DependencyLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  @Override
  public void start() {
    this.installDependencies();
    this.installFFmpeg();
    this.installVLC();
    this.installRTSP();
  }

  private void installFFmpeg() {
    try {
      new FFmpegDependency(this.core);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void installDependencies() {
    try {
      new LibraryDependencyManager(this.core);
    } catch (final IOException | ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private void installVLC() {
    try {
      new VLCDependency(this.core);
      this.loadNativeLibVLC();
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
      new SimpleRTSPServerDependency(this.core);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
