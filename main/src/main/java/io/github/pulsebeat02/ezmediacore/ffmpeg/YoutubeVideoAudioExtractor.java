package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.VideoDownloader;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.YoutubeVideoDownloader;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YoutubeVideoAudioExtractor implements YoutubeAudioExtractor {

  private final YoutubeVideoDownloader downloader;
  private final FFmpegAudioExtractor extractor;
  private final Path tempVideoPath;

  public YoutubeVideoAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final Path output) {
    this.tempVideoPath = core.getVideoPath().resolve(String.format("%s.mp4", UUID.randomUUID()));
    this.downloader = new YoutubeVideoDownloader(url, this.tempVideoPath);
    this.extractor = new FFmpegAudioExtractor(core, configuration, this.tempVideoPath, output);
  }

  public YoutubeVideoAudioExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final String fileName) {
    this.tempVideoPath = core.getVideoPath().resolve(String.format("%s.mp4", UUID.randomUUID()));
    this.downloader = new YoutubeVideoDownloader(url, this.tempVideoPath);
    this.extractor =
        new FFmpegAudioExtractor(
            core, configuration, this.tempVideoPath, core.getAudioPath().resolve(fileName));
  }

  @Override
  public void execute() {}

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    final boolean log = logger != null;
    if (log) {
      logger.accept("Downloading Video...");
    }
    this.downloader.downloadVideo(
        this.downloader.getVideo().getVideoFormats().get(0).getQuality(), true);
    if (log) {
      logger.accept("Finished downloading Video");
    }
    this.extractor.executeWithLogging(logger);
  }

  @Override
  public void executeAsync() {
    CompletableFuture.runAsync(this::execute);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsync(@NotNull final Executor executor) {
    return CompletableFuture.runAsync(this::execute, executor);
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger));
  }

  @Override
  public @NotNull CompletableFuture<Void> executeAsyncWithLogging(
      @NotNull final Consumer<String> logger, @NotNull final Executor executor) {
    return CompletableFuture.runAsync(() -> this.executeWithLogging(logger), executor);
  }

  @Override
  public void onStartAudioExtraction() {}

  @Override
  public void onFinishAudioExtraction() {}

  @Override
  public @NotNull VideoDownloader getDownloader() {
    return this.downloader;
  }

  @Override
  public @NotNull AudioExtractor getExtractor() {
    return this.extractor;
  }
}
