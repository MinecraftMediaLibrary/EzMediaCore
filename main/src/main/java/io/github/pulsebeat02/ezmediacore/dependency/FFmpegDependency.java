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

import io.github.pulsebeat02.emcinstallers.implementation.ffmpeg.FFmpegInstaller;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class FFmpegDependency {

  private final MediaLibraryCore core;
  private final Path folder;

  public FFmpegDependency(@NotNull final MediaLibraryCore core) throws IOException {
    this.core = core;
    this.folder = core.getDependencyPath().resolve("ffmpeg");
    this.start();
  }

  private void start() throws IOException {
    final Path path = FFmpegInstaller.create(this.folder).download(true);
    this.core.setFFmpegPath(path);
    Logger.info(Locale.BINARY_PATHS.build("FFmpeg", path));
  }

  public @NotNull Path getFolder() {
    return this.folder;
  }
}
