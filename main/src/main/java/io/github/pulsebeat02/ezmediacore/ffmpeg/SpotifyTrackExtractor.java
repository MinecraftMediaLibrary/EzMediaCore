package io.github.pulsebeat02.ezmediacore.ffmpeg;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.TrackDownloader;
import io.github.pulsebeat02.ezmediacore.playlist.youtube.SpotifyTrackDownloader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpotifyTrackExtractor implements SpotifyAudioExtractor {

  private final SpotifyTrackDownloader downloader;
  private final YoutubeVideoAudioExtractor extractor;
  private boolean cancelled;

  public SpotifyTrackExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final Path output)
      throws IOException {
    this.downloader = new SpotifyTrackDownloader(url, output);
    this.extractor = new YoutubeVideoAudioExtractor(core, configuration, url, output);
  }

  public SpotifyTrackExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final String fileName)
      throws IOException {
    this(core, configuration, url, core.getAudioPath().resolve(fileName));
  }

  @Override
  public void execute() {
    this.executeWithLogging(null);
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) {
    this.onStartAudioExtraction();
    final boolean log = logger != null;
    if (log) {
      logger.accept("Downloading Video...");
    }
    this.downloader.downloadVideo(
        this.downloader.getSearcher().getYoutubeVideo().getVideoFormats().get(0).getQuality(),
        true);
    if (log) {
      logger.accept("Finished downloading Video");
    }
    this.extractor.executeWithLogging(logger);
    this.onFinishAudioExtraction();
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
  public void cancelProcess() {
    this.onDownloadCancellation();
    this.cancelled = true;
    this.downloader.cancelDownload();
    this.extractor.cancelProcess();
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void onDownloadCancellation() {}

  @Override
  public void onStartAudioExtraction() {}

  @Override
  public void onFinishAudioExtraction() {}

  @Override
  public @NotNull TrackDownloader getTrackDownloader() {
    return this.downloader;
  }

  @Override
  public @NotNull YoutubeAudioExtractor getYoutubeExtractor() {
    return this.extractor;
  }
}
