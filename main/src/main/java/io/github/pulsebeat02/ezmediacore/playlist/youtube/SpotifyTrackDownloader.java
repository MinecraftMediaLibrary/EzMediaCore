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
package io.github.pulsebeat02.ezmediacore.playlist.youtube;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.response.Response;
import com.google.common.collect.BiMap;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.QuerySearch;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyTrack;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.Track;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.TrackDownloader;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.ResponseUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FilenameUtils;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SpotifyTrackDownloader implements TrackDownloader {

  private final SpotifyQuerySearch searcher;
  private final Track track;
  private final Path videoPath;
  private final AtomicBoolean cancelled;

  SpotifyTrackDownloader(@NotNull final Track track, @NotNull final Path videoPath) {
    checkNotNull(track, "Track cannot be null!");
    checkNotNull(videoPath, "Path cannot be null!");
    this.searcher = SpotifyQuerySearch.ofSpotifyQuerySearch(track);
    this.track = track;
    this.videoPath = videoPath;
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract("_, _ -> new")
  public static @NotNull SpotifyTrackDownloader ofSpotifyTrackDownloader(
      @NotNull final Track track, @NotNull final Path videoPath) {
    return new SpotifyTrackDownloader(track, videoPath);
  }

  @Contract("_, _ -> new")
  public static @NotNull SpotifyTrackDownloader ofSpotifyTrackDownloader(
      @NotNull final String url, @NotNull final Path videoPath)
      throws IOException, ParseException, SpotifyWebApiException {
    return ofSpotifyTrackDownloader(SpotifyTrack.ofSpotifyTrack(url), videoPath);
  }

  @Contract("_, _ -> new")
  public static @NotNull SpotifyTrackDownloader ofSpotifyTrackDownloader(
      @NotNull final MediaLibraryCore core, @NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return ofSpotifyTrackDownloader(
        SpotifyTrack.ofSpotifyTrack(url),
        core.getVideoPath().resolve("%s.mp4".formatted(UUID.randomUUID())));
  }

  @Override
  public void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite) {
    this.onStartVideoDownload();
    if (!this.cancelled.get()) {
      if (!this.makeVideoResponse(format, overwrite)) {
        this.onDownloadFailure();
      }
    }
    this.onFinishVideoDownload();
  }

  private boolean makeVideoResponse(@NotNull final VideoQuality format, final boolean overwrite) {
    final RequestVideoFileDownload request = this.createDownloadRequest(format, overwrite);
    for (int i = 0; i < 10; i++) {
      if (!this.cancelled.get()) {
        final Response<File> response =
            YoutubeProvider.getYoutubeDownloader().downloadVideoFile(request);
        final Optional<File> optional = ResponseUtils.getResponseResult(response);
        if (optional.isPresent()) {
          return true;
        }
      }
    }
    return false;
  }

  private @NotNull RequestVideoFileDownload createDownloadRequest(
      @NotNull final VideoQuality format, final boolean overwrite) {
    return new RequestVideoFileDownload(this.getFormat(format))
        .saveTo(this.videoPath.getParent().toFile())
        .renameTo(FilenameUtils.removeExtension(PathUtils.getName(this.videoPath)))
        .overwriteIfExists(overwrite);
  }

  private com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat(
      @NotNull final VideoQuality format) {
    final BiMap<VideoQuality, com.github.kiulian.downloader.model.videos.quality.VideoQuality>
        inverse = YoutubeVideoFormat.getVideoFormatMappings().inverse();
    return this.searcher.getRawVideo().getVideoInfo().videoFormats().stream()
        .filter(f -> f.videoQuality().equals(inverse.get(format)))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public void onStartVideoDownload() {}

  @Override
  public void onFinishVideoDownload() {}

  @Override
  public @NotNull Path getDownloadPath() {
    return this.videoPath;
  }

  @Override
  public @NotNull Track getTrack() {
    return this.track;
  }

  @Override
  public @NotNull QuerySearch getSearcher() {
    return this.searcher;
  }

  @Override
  public void cancelDownload() {
    this.onDownloadCancellation();
    this.cancelled.set(true);
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled.get();
  }

  @Override
  public void onDownloadCancellation() {}

  @Override
  public void onDownloadFailure() {
    throw new AssertionError(
        "Failed to retrieve Spotify track information from %s!".formatted(this.track.getUrl()));
  }
}
