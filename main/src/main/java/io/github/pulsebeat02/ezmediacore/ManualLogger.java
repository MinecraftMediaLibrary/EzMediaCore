package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ManualLogger implements CoreLogger {

  private final MediaLibraryCore core;
  private final Path emcPath;
  private final Path vlcPath;
  private final Path rtpPath;
  private final Path ffmpegPlayerPath;
  private final Path ffmpegStreamPath;

  private PrintWriter emc;
  private PrintWriter vlc;
  private PrintWriter rtp;
  private PrintWriter ffmpegPlayer;
  private PrintWriter ffmpegStream;

  ManualLogger(@NotNull final MediaLibraryCore core) {
    this.core = core;
    final Path path = core.getLibraryPath();
    this.emcPath = path.resolve("emc.log");
    this.vlcPath = path.resolve("vlc.log");
    this.rtpPath = path.resolve("rtp.log");
    this.ffmpegPlayerPath = path.resolve("ffmpeg.log");
    this.ffmpegStreamPath = path.resolve("ffmpeg-stream.log");
  }

  @Override
  public void start() throws IOException {
    this.createDirectories();
    this.createFiles();
    this.setPermissions();
    this.assignWriters();
  }

  @Override
  public void info(@NotNull final Object info) {
    this.directPrint("%d: [INFO] %s".formatted(System.currentTimeMillis(), info));
  }

  @Override
  public void warn(@NotNull final Object warning) {
    this.directPrint("%d: [WARN] %s".formatted(System.currentTimeMillis(), warning));
  }

  @Override
  public void error(@NotNull final Object error) {
    this.directPrint("%d: [ERROR] %s".formatted(System.currentTimeMillis(), error));
  }

  @Override
  public void vlc(@NotNull final String line) {
    this.internalDirectPrint(this.vlc, line);
  }

  @Override
  public void ffmpegPlayer(@NotNull final String line) {
    this.internalDirectPrint(this.ffmpegPlayer, line);
  }

  @Override
  public void ffmpegStream(@NotNull final String line) {
    this.internalDirectPrint(this.ffmpegStream, line);
  }

  @Override
  public void rtp(@NotNull final String line) {
    this.internalDirectPrint(this.rtp, line);
  }

  private void directPrint(@NotNull final String line) {
    this.internalDirectPrint(this.emc, line);
  }

  private void internalDirectPrint(@NotNull final PrintWriter writer, @NotNull final String line) {
    CompletableFuture.runAsync(this.print(writer, line), ExecutorProvider.LOGGER_POOL);
  }

  @Contract(pure = true)
  private @NotNull Runnable print(@NotNull final PrintWriter writer, @NotNull final String line) {
    return () -> {
      writer.write("%s\n".formatted(line));
      writer.flush();
    };
  }

  private void assignWriters() throws IOException {
    this.emc = new PrintWriter(Files.newBufferedWriter(this.emcPath), true);
    this.vlc = new PrintWriter(Files.newBufferedWriter(this.vlcPath), true);
    this.rtp = new PrintWriter(Files.newBufferedWriter(this.rtpPath), true);
    this.ffmpegPlayer = new PrintWriter(Files.newBufferedWriter(this.ffmpegPlayerPath), true);
    this.ffmpegStream = new PrintWriter(Files.newBufferedWriter(this.ffmpegStreamPath), true);
  }

  private void setPermissions() throws IOException {
    if (this.isUnix()) {
      Runtime.getRuntime()
          .exec(new String[] {"chmod", "-R", "777", this.core.getLibraryPath().toString()});
    }
  }

  private boolean isUnix() {
    final String os = System.getProperty("os.name").toLowerCase();
    return os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix");
  }

  private void createDirectories() {
    try {
      Files.createDirectories(this.core.getLibraryPath());
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void createFiles() {
    Set.of(this.emcPath, this.vlcPath, this.rtpPath, this.ffmpegPlayerPath, this.ffmpegStreamPath)
        .forEach(this::createFile);
  }

  private void createFile(@NotNull final Path file) {
    try {
      FileUtils.createIfNotExists(file);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void close() {
    Set.of(this.emc, this.vlc, this.rtp, this.ffmpegPlayer, this.ffmpegStream)
        .forEach(this::shutdown);
  }

  private void shutdown(@NotNull final PrintWriter writer) {
    writer.flush();
    writer.close();
  }
}
