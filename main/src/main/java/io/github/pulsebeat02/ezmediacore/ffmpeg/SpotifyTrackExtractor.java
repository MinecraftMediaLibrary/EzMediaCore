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
  public CompletableFuture<Void> executeAsync() {
    return CompletableFuture.runAsync(this::execute);
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
