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
package io.github.pulsebeat02.ezmediacore.installers.vlc.installation;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class WinInstallationStrategy extends ManualInstallationStrategy {

  private static final String VLC_TEMP = "temp-vlc";
  private static final String VLC_APP = "VLC";

  public WinInstallationStrategy(
      final VLCInstaller installer) {
    super(installer);
  }

  @Override
  public Optional<Path> getInstalledPath() {
    final VLCInstaller installer = this.getInstaller();
    final Path path = installer.getPath();
    final Path parent = path.getParent();
    final Path vlc = parent.resolve(VLC_APP);
    return Files.exists(vlc) ? Optional.of(vlc) : Optional.empty();
  }

  @Override
  public Path execute() throws IOException {

    final VLCInstaller installer = this.getInstaller();
    final Path zip = installer.getPath();
    final Path parent = zip.getParent();
    final Path temp = parent.resolve(VLC_TEMP);
    final Path path = parent.resolve(VLC_APP);
    this.extractArchive(zip, temp);
    this.deleteFile(zip);
    this.moveFiles(temp, path);
    this.deleteFile(temp);

    return path;
  }

  private void extractArchive(final Path zip, final Path temp) throws IOException {
    final File file = zip.toFile();
    try (final ZipFile zipFile = new ZipFile(file)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final Path entryDestination = temp.resolve(entry.getName());
        this.handleZipEntry(entry, entryDestination, zipFile);
      }
    }
  }

  private void handleZipEntry(final ZipEntry entry, final Path entryDestination, final ZipFile zipFile) throws IOException {
    if (entry.isDirectory()) {
      this.createDirectories(entryDestination);
    } else {
      this.createDirectories(entryDestination.getParent());
      final File dest = entryDestination.toFile();
      try (final InputStream in = zipFile.getInputStream(entry);
           final OutputStream out = new FileOutputStream(dest)) {
        ByteStreams.copy(in, out);
      }
    }
  }

  private void createDirectories(final Path path) throws IOException {
    if (Files.notExists(path)) {
      Files.createDirectories(path);
    }
  }

  private void moveFiles(final Path temp, final Path dest) throws IOException {
    final String name = "vlc-" + VLCInstaller.VERSION;
    final Path resolve = temp.resolve(name);
    Files.move(resolve, dest);
  }
}
