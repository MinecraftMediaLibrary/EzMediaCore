/**
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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.platform.win;

import com.google.common.io.ByteStreams;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.VLCInstaller;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.ManualInstallationStrategy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class WinInstallationStrategy extends ManualInstallationStrategy {

  public WinInstallationStrategy(
      final VLCInstaller installer) {
    super(installer);
  }

  @Override
  public Optional<Path> getInstalledPath() {
    final Path path = this.getInstaller().getPath().getParent().resolve("VLC");
    return Files.exists(path) ? Optional.of(path) : Optional.empty();
  }

  @Override
  public Path execute() throws IOException {
    final Path zip = this.getInstaller().getPath();
    final Path parent = zip.getParent();
    final Path temp = parent.resolve("temp-vlc");
    final Path path = parent.resolve("VLC");
    this.extractArchive(zip, temp);
    this.deleteFile(zip);
    this.moveFiles(temp, path);
    this.deleteFile(temp);
    return path;
  }

  private void extractArchive(final Path zip, final Path temp) throws IOException {
    try (final ZipFile zipFile = new ZipFile(zip.toFile())) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final Path entryDestination = temp.resolve(entry.getName());
        if (entry.isDirectory()) {
          this.createDirectories(entryDestination);
        } else {
          this.createDirectories(entryDestination.getParent());
          try (final InputStream in = zipFile.getInputStream(entry);
              final OutputStream out = new FileOutputStream(entryDestination.toFile())) {
            ByteStreams.copy(in, out);
          }
        }
      }
    }
  }

  private void createDirectories(final Path path) throws IOException {
    if (Files.notExists(path)) {
      Files.createDirectories(path);
    }
  }

  private void moveFiles(final Path temp, final Path dest) throws IOException {
    Files.move(temp.resolve(String.format("vlc-%s", VLCInstaller.VERSION)), dest);
  }
}
