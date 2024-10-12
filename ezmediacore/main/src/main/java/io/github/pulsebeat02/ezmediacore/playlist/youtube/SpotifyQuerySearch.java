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

import io.github.pulsebeat02.ezmediacore.playlist.spotify.QuerySearch;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyTrack;
import io.github.pulsebeat02.ezmediacore.playlist.spotify.Track;
import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

public class SpotifyQuerySearch implements QuerySearch {

  private final Track track;
  private final YoutubeVideo video;
  private final String query;

  SpotifyQuerySearch( final Track track) {
    checkNotNull(track, "Track cannot be null!");
    this.track = track;
    this.query = this.constructInternalQuery();
    this.video = this.getInternalVideo();
  }

  @Contract("_ -> new")
  public static  SpotifyQuerySearch ofSpotifyQuerySearch( final Track track) {
    return new SpotifyQuerySearch(track);
  }

  @Contract("_ -> new")
  public static  SpotifyQuerySearch ofSpotifyQuerySearch( final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return ofSpotifyQuerySearch(SpotifyTrack.ofSpotifyTrack(url));
  }

  private  String constructInternalQuery() {
    return "%s %s".formatted(this.track.getName(), this.track.getArtists().get(0));
  }

  @Contract(" -> new")
  private  YoutubeVideo getInternalVideo() {
    return YoutubeVideo.ofYoutubeVideo(
        MediaExtractionUtils.getFirstResultVideoExceptionally(this.query));
  }

  @Override
  public  Video getYoutubeVideo() {
    return this.video;
  }

  @Override
  public  String getSearchQuery() {
    return this.query;
  }

  @Override
  public  Track getTrack() {
    return this.track;
  }

  YoutubeVideo getRawVideo() {
    return this.video;
  }
}
