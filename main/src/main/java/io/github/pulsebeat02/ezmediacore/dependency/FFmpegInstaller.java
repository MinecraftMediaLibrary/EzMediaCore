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

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.Diagnostic;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.task.CommandTask;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import io.github.pulsebeat02.ezmediacore.utility.HashingUtils;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public final class FFmpegInstaller {

  private final MediaLibraryCore core;
  private final Path folder;
  private final Path hashFile;
  private String hash;
  private Path executable;

  public FFmpegInstaller(@NotNull final MediaLibraryCore core) throws IOException {
    this.core = core;
    final Path path = core.getDependencyPath();
    this.folder = path.resolve("ffmpeg");
    this.hashFile = this.folder.resolve(".ffmpeg-hash");
  }

  public void start() throws IOException {
    this.createFiles();
    this.download();
    this.writeHashes();
    Logger.info("FFmpeg Path: %s".formatted(this.executable));
  }

  private void createFiles() throws IOException {
    Files.createDirectories(this.folder);
    if (!FileUtils.createIfNotExists(this.hashFile)) {
      this.hash = FileUtils.getFirstLine(this.hashFile);
    }
  }

  private void writeHashes() throws IOException {
    try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.hashFile))) {
      writer.println(this.hash);
    }
  }

  private void download() throws IOException {
    final Optional<Path> optional = this.detectExecutable(this.folder);
    if (optional.isPresent()) {
      this.executable = optional.get();
      return;
    }
    this.executable = this.internalDownload();
    this.setPermissions(this.executable);
    this.hash = HashingUtils.getHash(this.executable);
  }

  private @NotNull Path internalDownload() throws IOException {
    final Diagnostic diagnostic = this.core.getDiagnostics();
    final String url = diagnostic.getFFmpegUrl();
    final Path download = this.folder.resolve(FilenameUtils.getName(new URL(url).getPath()));
    FileUtils.createIfNotExists(download);
    DependencyUtils.downloadFile(download, url);
    return download;
  }

  private void setPermissions(@NotNull final Path download) {
    final OSType type = this.core.getDiagnostics().getSystem().getOSType();
    if (type == OSType.MAC || type == OSType.UNIX) {
      try {
        new CommandTask("chmod", "-R", "777", download.toAbsolutePath().toString()).run();
      } catch (final IOException e) {
        Logger.info(
            "User doesn't have enough permissions to execute file! Copying file to directory and renaming!");
        this.copyAndRenameFile(download);
      }
    }
  }

  private void copyAndRenameFile(@NotNull final Path path) {
    final String name = PathUtils.getName(path);
    final Path parent = path.getParent();
    final Path temp = parent.resolve("temp-ffmpeg");
    try {
      Files.copy(path, temp, StandardCopyOption.REPLACE_EXISTING);
      Files.delete(path);
      Files.move(temp, parent.resolveSibling(name));
    } catch (final IOException e) {
      Logger.info("A severe error has occurred while trying to bypass execute permissions!");
      e.printStackTrace();
    }
  }

  private @NotNull Optional<Path> detectExecutable(@NotNull final Path folder) throws IOException {
    try (final Stream<Path> files = Files.walk(folder, 1)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> HashingUtils.getHash(path).equals(this.hash))
          .findAny();
    }
  }

  public @NotNull Path getFolder() {
    return this.folder;
  }

  public @NotNull Path getHashFile() {
    return this.hashFile;
  }

  public @NotNull String getHash() {
    return this.hash;
  }

  public @NotNull Path getExecutable() {
    return this.executable;
  }
}
