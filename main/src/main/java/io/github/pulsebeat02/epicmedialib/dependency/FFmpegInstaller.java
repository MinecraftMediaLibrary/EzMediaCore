package io.github.pulsebeat02.epicmedialib.dependency;

import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.analysis.Diagnostic;
import io.github.pulsebeat02.epicmedialib.analysis.OSType;
import io.github.pulsebeat02.epicmedialib.task.CommandTask;
import io.github.pulsebeat02.epicmedialib.utility.DependencyUtils;
import io.github.pulsebeat02.epicmedialib.utility.FileUtils;
import io.github.pulsebeat02.epicmedialib.utility.HashingUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
    this.hashFile = path.resolve(".relocated-cache");
  }

  public void start() throws IOException {
    createFiles();
    download();
    writeHashes();
    Logger.info(String.format("FFmpeg Path: %s", this.executable));
  }

  private void createFiles() throws IOException {
    Files.createDirectories(this.folder);
    if (!FileUtils.createFileIfNotExists(this.hashFile)) {
      this.hash = FileUtils.getFirstLine(this.hashFile);
    }
  }

  private void writeHashes() throws IOException {
    try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.hashFile))) {
      writer.println(this.hash);
    }
  }

  private void download() throws IOException {

    final Optional<Path> optional = detectExecutable(this.folder);
    if (optional.isPresent()) {
      this.executable = optional.get();
      return;
    }

    final Diagnostic diagnostic = this.core.getDiagnostics();
    final OSType type = diagnostic.getSystem().getOSType();
    final String url = diagnostic.getFFmpegUrl();
    final Path download = this.folder.resolve(FilenameUtils.getName(new URL(url).getPath()));
    FileUtils.createFileIfNotExists(download);

    DependencyUtils.downloadFile(download, url);
    if (type == OSType.MAC || type == OSType.UNIX) {
      new CommandTask("chmod", "-R", "777", download.toAbsolutePath().toString()).run();
    }

    this.executable = download;
    this.hash = HashingUtils.getHash(this.executable);
  }

  private Optional<Path> detectExecutable(@NotNull final Path folder) throws IOException {
    try (final Stream<Path> files = Files.walk(folder)) {
      return files.filter(path -> HashingUtils.getHash(path).equals(this.hash)).findAny();
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
