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
package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingFunction;
import io.github.pulsebeat02.ezmediacore.throwable.DeadResourceLinkException;
import io.github.pulsebeat02.ezmediacore.utility.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

public class SpotifyTrack implements Track {

  private final com.wrapper.spotify.model_objects.specification.Track track;
  private final String url;
  private final List<Artist> artists;

  public SpotifyTrack(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    this.url = url;
    this.track =
        SpotifyProvider.getSpotifyApi()
            .getTrack(
                MediaExtractionUtils.getSpotifyID(url)
                    .orElseThrow(() -> new DeadResourceLinkException(url)))
            .build()
            .execute();
    this.artists =
        Arrays.stream(this.track.getArtists())
            .map(ThrowingFunction.unchecked(SpotifyArtist::new))
            .collect(Collectors.toList());
  }

  @Override
  public @NotNull String getId() {
    return this.track.getId();
  }

  @Override
  public @NotNull String getName() {
    return this.track.getName();
  }

  @Override
  public @NotNull List<Artist> getArtists() {
    return this.artists;
  }

  @Override
  public @NotNull Map<String, String> getExternalUrls() {
    return this.track.getExternalUrls().getExternalUrls();
  }

  @Override
  public @NotNull String getPreviewUrl() {
    return this.track.getPreviewUrl();
  }

  @Override
  public int getDiscNumber() {
    return this.track.getDiscNumber();
  }

  @Override
  public int getDuration() {
    return this.track.getDurationMs();
  }

  @Override
  public boolean isExplicit() {
    return this.track.getIsExplicit();
  }

  @Override
  public boolean isPlayable() {
    return this.track.getIsPlayable();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @NotNull com.wrapper.spotify.model_objects.specification.Track getTrack() {
    return this.track;
  }
}
