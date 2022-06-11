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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class SpotifyOGGTrackExtractor extends FFmpegCommandExecutor
    implements SpotifyAudioExtractor {

  private final SpotifyTrackDownloader downloader;
  private final YoutubeOGGAudioExtractor extractor;
  private final AtomicBoolean cancelled;

  SpotifyOGGTrackExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final Path output)
      throws IOException, ParseException, SpotifyWebApiException {
    super(core);
    this.downloader = SpotifyTrackDownloader.ofSpotifyTrackDownloader(url, output);
    this.extractor =
        YoutubeOGGAudioExtractor.ofYoutubeVideoAudioExtractor(core, configuration, url, output);
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull SpotifyOGGTrackExtractor ofSpotifyTrackExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final Path output)
      throws IOException, ParseException, SpotifyWebApiException {
    return new SpotifyOGGTrackExtractor(core, configuration, url, output);
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull SpotifyOGGTrackExtractor ofSpotifyTrackExtractor(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final String url,
      @NotNull final String fileName)
      throws IOException, ParseException, SpotifyWebApiException {
    return ofSpotifyTrackExtractor(core, configuration, url, core.getAudioPath().resolve(fileName));
  }

  @Override
  public void executeWithLogging(@Nullable final Consumer<String> logger) throws IOException {
    this.onStartAudioExtraction();
    this.downloader.downloadVideo(
        this.downloader.getSearcher().getYoutubeVideo().getVideoFormats().get(0).getQuality(),
        true);
    this.extractor.executeWithLogging(logger);
    this.onFinishAudioExtraction();
  }

  @Override
  public void close() throws InterruptedException {
    this.onDownloadCancellation();
    this.cancelled.set(true);
    this.downloader.cancelDownload();
    this.extractor.close();
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
