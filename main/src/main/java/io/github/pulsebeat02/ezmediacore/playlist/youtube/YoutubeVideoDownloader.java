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
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResponseUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class YoutubeVideoDownloader implements VideoDownloader {

  private final YoutubeVideo video;
  private final Path videoPath;
  private boolean cancelled;

  public YoutubeVideoDownloader(@NotNull final String url, @NotNull final Path videoPath) {
    this(new YoutubeVideo(url), videoPath);
  }

  public YoutubeVideoDownloader(@NotNull final YoutubeVideo video, @NotNull final Path videoPath) {
    this.video = video;
    this.videoPath = videoPath;
    this.cancelled = false;
  }

  public YoutubeVideoDownloader(
      @NotNull final MediaLibraryCore core, @NotNull final YoutubeVideo video) {
    this.video = video;
    this.videoPath = core.getVideoPath().resolve("%s.mp4".formatted(UUID.randomUUID()));
    this.cancelled = false;
  }

  public YoutubeVideoDownloader(
      @NotNull final MediaLibraryCore core,
      @NotNull final YoutubeVideo video,
      @NotNull final String fileName) {
    this.video = video;
    this.videoPath = core.getVideoPath().resolve(fileName);
    this.cancelled = false;
  }

  public YoutubeVideoDownloader(
      @NotNull final MediaLibraryCore core,
      @NotNull final String url,
      @NotNull final String fileName) {
    this(core, new YoutubeVideo(url), fileName);
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
    return this.video.getVideoInfo().videoFormats().stream()
        .filter(
            f ->
                f.videoQuality()
                    .equals(YoutubeVideoFormat.getVideoFormatMappings().inverse().get(format)))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public void onStartVideoDownload() {
  }

  @Override
  public void onFinishVideoDownload() {
  }

  @Override
  public void onDownloadFailure() {
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
  public @NotNull Video getVideo() {
    return this.video;
  }

  @Override
  public @NotNull Path getDownloadPath() {
    return this.videoPath;
  }

  @Override
  public void onDownloadCancellation() {
  }
}
