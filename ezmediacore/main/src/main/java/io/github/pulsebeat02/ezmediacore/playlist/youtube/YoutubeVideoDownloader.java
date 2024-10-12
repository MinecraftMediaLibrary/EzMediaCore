/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import com.google.common.io.Files;
import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Contract;


public class YoutubeVideoDownloader implements VideoDownloader {

  private final YoutubeVideo video;
  private final Path videoPath;
  private final AtomicBoolean cancelled;

  YoutubeVideoDownloader( final YoutubeVideo video,  final Path videoPath) {
    checkNotNull(video, "YoutubeVideo cannot be null!");
    checkNotNull(videoPath, "Video path cannot be null!");
    this.video = video;
    this.videoPath = videoPath;
    this.cancelled = new AtomicBoolean(false);
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static  YoutubeVideoDownloader ofYoutubeVideoDownloader(
       final YoutubeVideo video,  final Path videoPath) {
    return new YoutubeVideoDownloader(video, videoPath);
  }

  @Contract("_, _ -> new")
  public static  YoutubeVideoDownloader ofYoutubeVideoDownloader(
       final EzMediaCore core,  final YoutubeVideo video) {
    return ofYoutubeVideoDownloader(
        video, core.getVideoPath().resolve("%s.mp4".formatted(UUID.randomUUID())));
  }

  @Contract("_, _, _ -> new")
  public static  YoutubeVideoDownloader ofYoutubeVideoDownloader(
       final EzMediaCore core,
       final YoutubeVideo video,
       final String fileName) {
    return ofYoutubeVideoDownloader(video, core.getVideoPath().resolve(fileName));
  }

  @Contract("_, _, _ -> new")
  public static  YoutubeVideoDownloader ofYoutubeVideoDownloader(
       final EzMediaCore core,
       final String url,
       final String fileName) {
    return ofYoutubeVideoDownloader(core, YoutubeVideo.ofYoutubeVideo(url), fileName);
  }

  @Contract("_, _ -> new")
  public static  YoutubeVideoDownloader ofYoutubeVideoDownloader(
       final String url,  final Path videoPath) {
    return ofYoutubeVideoDownloader(YoutubeVideo.ofYoutubeVideo(url), videoPath);
  }

  @Override
  public void downloadVideo( final VideoQuality format, final boolean overwrite) {
    this.onStartVideoDownload();
    if (!this.cancelled.get() && !this.makeVideoResponse(format, overwrite)) {
      this.onDownloadFailure();
    }
    this.onFinishVideoDownload();
  }

  private boolean makeVideoResponse( final VideoQuality format, final boolean overwrite) {

    final RequestVideoFileDownload request = this.createDownloadRequest(format, overwrite);

    for (int i = 0; i < 10; i++) {

      if (this.cancelled.get()) {
        continue;
      }

      final Response<File> response =
          YoutubeProvider.getYoutubeDownloader().downloadVideoFile(request);
      final Optional<File> optional = ResponseUtils.getResponseResult(response);
      if (optional.isPresent()) {
        return true;
      }
    }

    return false;
  }

  private  RequestVideoFileDownload createDownloadRequest(
       final VideoQuality format, final boolean overwrite) {
    return new RequestVideoFileDownload(this.getFormat(format))
        .saveTo(this.videoPath.getParent().toFile())
        .renameTo(Files.getNameWithoutExtension(PathUtils.getName(this.videoPath)))
        .overwriteIfExists(overwrite);
  }

  private com.github.kiulian.downloader.model.videos.formats.VideoFormat getFormat(
       final VideoQuality format) {
    final BiMap<VideoQuality, com.github.kiulian.downloader.model.videos.quality.VideoQuality>
        inverse = YoutubeVideoFormat.getVideoFormatMappings().inverse();
    return this.video.getVideoInfo().videoFormats().stream()
        .filter(f -> f.videoQuality().equals(inverse.get(format)))
        .findAny()
        .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public void onStartVideoDownload() {}

  @Override
  public void onFinishVideoDownload() {}

  @Override
  public void onDownloadFailure() {}

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
  public  Video getVideo() {
    return this.video;
  }

  @Override
  public  Path getDownloadPath() {
    return this.videoPath;
  }

  @Override
  public void onDownloadCancellation() {
    throw new AssertionError(
        "Failed to download Youtube video from %s!".formatted(this.video.getUrl()));
  }
}
