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
package io.github.pulsebeat02.ezmediacore.playlist.spotify;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class SpotifyTrack implements Track {

  private final String url;
  private final List<Artist> artists;

  private final se.michaelthelin.spotify.model_objects.specification.Track track;

  SpotifyTrack( final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    checkNotNull(url, "URL cannot be null!");
    this.url = url;
    this.track = this.getInternalTrack();
    this.artists = this.getInternalArtists();
  }

  @Contract("_ -> new")
  public static  SpotifyTrack ofSpotifyTrack( final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return new SpotifyTrack(url);
  }

  private  se.michaelthelin.spotify.model_objects.specification.Track getInternalTrack()
      throws IOException, ParseException, SpotifyWebApiException {
    return SpotifyProvider.getSpotifyApi()
        .getTrack(MediaExtractionUtils.getSpotifyIDExceptionally(this.url))
        .build()
        .execute();
  }

  private  List<Artist> getInternalArtists() {
    return Stream.of(this.track.getArtists())
        .map(this::getInternalArtist)
        .collect(Collectors.toList());
  }

  @Contract("_ -> new")
  private  SpotifyArtist getInternalArtist(final ArtistSimplified simplified) {
    try {
      return new SpotifyArtist(simplified);
    } catch (final IOException | ParseException | SpotifyWebApiException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public  String getId() {
    return this.track.getId();
  }

  @Override
  public  String getName() {
    return this.track.getName();
  }

  @Override
  public  List<Artist> getArtists() {
    return this.artists;
  }

  @Override
  public  Map<String, String> getExternalUrls() {
    return this.track.getExternalUrls().getExternalUrls();
  }

  @Override
  public  String getPreviewUrl() {
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
  public  String getUrl() {
    return this.url;
  }

  
  se.michaelthelin.spotify.model_objects.specification.Track getTrack() {
    return this.track;
  }
}
