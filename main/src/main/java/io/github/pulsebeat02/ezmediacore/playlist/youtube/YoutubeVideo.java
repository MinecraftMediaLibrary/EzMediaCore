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

import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.throwable.RequestFailureException;
import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import io.github.pulsebeat02.ezmediacore.utility.media.ResponseUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class YoutubeVideo implements Video {

  private final String url;
  private final String id;
  private final VideoInfo video;
  private final VideoDetails details;
  private final List<VideoFormat> videoFormats;
  private final List<AudioFormat> audioFormats;

  public YoutubeVideo(@NotNull final String url) {
    this.url = url;
    this.id =
        MediaExtractionUtils.getYoutubeID(url)
            .orElseThrow(() -> new DeadResourceLinkException(url));
    this.video = this.getVideoResponse(0);
    this.details = this.video.details();
    this.videoFormats =
        this.video.videoFormats().stream()
            .map(YoutubeVideoFormat::new)
            .collect(Collectors.toList());
    this.audioFormats =
        this.video.audioFormats().stream()
            .map(YoutubeAudioFormat::new)
            .collect(Collectors.toList());
  }

  private VideoInfo getVideoResponse(final int tries) {
    if (tries == 10) {
      throw new RequestFailureException(
          "Failed to retrieve video information from url %s!".formatted(this.url));
    }
    final int num = tries + 1;
    return ResponseUtils.getResponseResult(
            YoutubeProvider.getYoutubeDownloader().getVideoInfo(new RequestVideoInfo(this.id)))
        .orElseGet(() -> this.getVideoResponse(num));
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull String getId() {
    return this.details.videoId();
  }

  @Override
  public @NotNull String getLiveUrl() {
    return this.details.liveUrl();
  }

  @Override
  public @NotNull List<String> getKeywords() {
    return this.details.keywords();
  }

  @Override
  public @NotNull List<VideoFormat> getVideoFormats() {
    return this.videoFormats;
  }

  @Override
  public @NotNull List<AudioFormat> getAudioFormats() {
    return this.audioFormats;
  }

  @Override
  public long getViewCount() {
    return this.details.viewCount();
  }

  @Override
  public int getAverageRating() {
    return this.details.averageRating();
  }

  @Override
  public boolean isLiveContent() {
    return this.details.isLiveContent();
  }

  @NotNull
  VideoInfo getVideoInfo() {
    return this.video;
  }
}
