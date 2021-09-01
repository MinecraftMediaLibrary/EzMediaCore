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
package io.github.pulsebeat02.ezmediacore.vlc.os;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.vlc.NativeBinarySearch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SilentInstallation implements SilentInstallationProvider {

  private final MediaLibraryCore core;
  private final Path directory;
  private Path installation;

  public SilentInstallation(@NotNull final MediaLibraryCore core, @NotNull final Path directory) {
    this.core = core;
    this.directory = directory;
  }

  @Override
  public @NotNull Path getDirectory() {
    return this.directory;
  }

  @Override
  public @NotNull Optional<Path> getInstallationPath() {
    return Optional.ofNullable(this.installation);
  }

  @Override
  public void setInstallationPath(@Nullable final Path path) {
    this.installation = path;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public void deleteArchive(@NotNull final Path archive) {
    try {
      Files.delete(archive);
      Logger.info("Archive successfully deleted.");
    } catch (final IOException e) {
      Logger.error("A severe I/O error occurred from deleting the archive file!");
      e.printStackTrace();
    }
  }

  @Override
  public void loadNativeBinaries() throws IOException {
    new NativeBinarySearch(this.getCore(), this.installation).search();
  }
}
