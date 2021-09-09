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

import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.QuerySearch;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyTrack;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.Track;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.TrackDownloader;
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResponseUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class SpotifyTrackDownloader implements TrackDownloader {

  private final SpotifyQuerySearch searcher;
  private final Track track;
  private final Path videoPath;
  private boolean cancelled;

  public SpotifyTrackDownloader(@NotNull final Track track, @NotNull final Path videoPath)
      throws IOException {
    this.searcher = new SpotifyQuerySearch(track);
    this.track = track;
    this.videoPath = videoPath;
    this.cancelled = false;
  }

  public SpotifyTrackDownloader(@NotNull final String url, @NotNull final Path videoPath)
      throws IOException {
    this(new SpotifyTrack(url), videoPath);
  }

  public SpotifyTrackDownloader(@NotNull final MediaLibraryCore core, @NotNull final String url)
      throws IOException {
    this(new SpotifyTrack(url), core.getVideoPath().resolve("%s.mp4".formatted(UUID.randomUUID())));
  }

  @Override
  public void downloadVideo(@NotNull final VideoQuality format, final boolean overwrite) {
    this.onStartVideoDownload();
    if (!this.cancelled) {
      this.internalDownload(
          new RequestVideoFileDownload(this.getFormat(format))
              .saveTo(this.videoPath.getParent().toFile())
              .renameTo(FilenameUtils.removeExtension(PathUtils.getName(this.videoPath)))
              .overwriteIfExists(overwrite));

      if (Files.notExists(this.videoPath)) {
        this.onDownloadFailure();
      }
    }
    this.onFinishVideoDownload();
  }

  private void internalDownload(@NotNull final RequestVideoFileDownload download) {
    if (ResponseUtils.getResponseResult(
                YoutubeProvider.getYoutubeDownloader().downloadVideoFile(download))
            .isEmpty()
        && !this.cancelled) {
      this.internalDownload(download);
    }
  }

  private com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat(
      @NotNull final VideoQuality format) {
    return this.searcher.getRawVideo().getVideoInfo().videoFormats().stream()
        .filter(
            f ->
                f.videoQuality()
                    .equals(YoutubeVideoFormat.getVideoFormatMappings().inverse().get(format)))
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
    this.cancelled = true;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void onDownloadCancellation() {}

  @Override
  public void onDownloadFailure() {}
}
