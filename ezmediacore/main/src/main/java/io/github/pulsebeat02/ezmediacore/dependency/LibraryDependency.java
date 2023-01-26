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
package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public abstract sealed class LibraryDependency permits DitherDependencyManager,
    FFmpegDependencyManager, LibraryDependencyManager, SimpleRTSPServerDependencyManager,
    VLCDependencyManager {

  private final MediaLibraryCore core;

  LibraryDependency(@NotNull final MediaLibraryCore core) throws IOException {
    this.core = core;
  }

  public abstract void start() throws IOException;

  public abstract void onInstallation(@NotNull final Path path);

  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
