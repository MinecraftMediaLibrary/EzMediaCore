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

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.QuerySearch;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyTrack;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.Track;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyQuerySearch implements QuerySearch {

  private final Track track;
  private final YoutubeVideo video;
  private final String query;

  public SpotifyQuerySearch(@NotNull final Track track) {
    this.track = track;
    final String query = "%s %s".formatted(track.getName(), track.getArtists().get(0));
    this.video =
        new YoutubeVideo(
            MediaExtractionUtils.getFirstResultVideo(query)
                .orElseThrow(() -> new DeadResourceLinkException(track.getId())));
    this.query = query;
  }

  public SpotifyQuerySearch(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this(new SpotifyTrack(url));
  }

  @Override
  public @NotNull Video getYoutubeVideo() {
    return this.video;
  }

  @Override
  public @NotNull String getSearchQuery() {
    return this.query;
  }

  @Override
  public @NotNull Track getTrack() {
    return this.track;
  }

  YoutubeVideo getRawVideo() {
    return this.video;
  }
}
