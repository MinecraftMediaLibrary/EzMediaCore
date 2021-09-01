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

import com.github.kiulian.downloader.downloader.request.RequestPlaylistInfo;
import com.github.kiulian.downloader.model.playlist.PlaylistDetails;
import com.github.kiulian.downloader.model.playlist.PlaylistInfo;
import io.github.pulsebeat02.ezmediacore.throwable.UnknownPlaylistException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import io.github.pulsebeat02.ezmediacore.utility.ResponseUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.jcodec.codecs.mjpeg.tools.AssertionException;
import org.jetbrains.annotations.NotNull;

public class YoutubePlaylist implements Playlist {

  private final String url;
  private final String id;
  private final PlaylistInfo info;
  private final PlaylistDetails details;
  private final List<Video> videos;

  public YoutubePlaylist(@NotNull final String url) {
    this.url = url;
    this.id =
        MediaExtractionUtils.getYoutubeID(url).orElseThrow(() -> new UnknownPlaylistException(url));
    this.info = this.getPlaylistResponse(0);
    this.details = this.info.details();
    this.videos =
        this.info.videos().parallelStream()
            .map(link -> new YoutubeVideo(link.videoId()))
            .collect(Collectors.toList());
  }

  private PlaylistInfo getPlaylistResponse(final int tries) {
    if (tries == 10) {
      throw new AssertionException(
          "Request failure with video ID %s! Please try again.".formatted(this.id));
    }
    final int num = tries + 1;
    return ResponseUtils.getResponseResult(
            YoutubeProvider.getYoutubeDownloader()
                .getPlaylistInfo(new RequestPlaylistInfo(this.id)))
        .orElseGet(() -> this.getPlaylistResponse(num));
  }

  @Override
  public @NotNull String getAuthor() {
    return this.details.author();
  }

  @Override
  public int getVideoCount() {
    return this.details.videoCount();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull String getName() {
    return this.details.title();
  }

  @Override
  public @NotNull List<Video> getVideos() {
    return this.videos;
  }

  @Override
  public long getViewCount() {
    return this.details.viewCount();
  }

  @NotNull
  PlaylistInfo getPlaylistInfo() {
    return this.info;
  }

  @NotNull
  PlaylistDetails getPlaylistDetails() {
    return this.details;
  }
}
