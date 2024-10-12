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
import java.util.Map;
import java.util.stream.Stream;

import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

public class SpotifyArtist implements Artist {

  private final se.michaelthelin.spotify.model_objects.specification.Artist artist;
  private final String url;
  private final Avatar[] avatars;

  SpotifyArtist( final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    checkNotNull(url, "URL cannot be null!");
    this.url = url;
    this.artist = this.getInternalArtist();
    this.avatars = this.getInternalAvatars();
  }

  SpotifyArtist( final ArtistSimplified simplified)
      throws IOException, ParseException, SpotifyWebApiException {
    this(simplified.getUri());
  }

  @Contract("_ -> new")
  public static  SpotifyArtist ofSpotifyArtist( final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return new SpotifyArtist(url);
  }

  private  se.michaelthelin.spotify.model_objects.specification.Artist getInternalArtist()
      throws IOException, ParseException, SpotifyWebApiException {
    return SpotifyProvider.getSpotifyApi()
        .getArtist(MediaExtractionUtils.getSpotifyIDExceptionally(this.url))
        .build()
        .execute();
  }

  private  Avatar  [] getInternalAvatars() {
    return Stream.of(this.artist.getImages()).map(SpotifyAvatar::new).toArray(SpotifyAvatar[]::new);
  }

  @Override
  public  String getId() {
    return this.artist.getId();
  }

  @Override
  public  String getName() {
    return this.artist.getName();
  }

  @Override
  public  Map<String, String> getExternalUrls() {
    return this.artist.getExternalUrls().getExternalUrls();
  }

  @Override
  public int getTotalFollowers() {
    return this.artist.getFollowers().getTotal();
  }

  @Override
  public int getPopularity() {
    return this.artist.getPopularity();
  }

  @Override
  public  String[] getGenres() {
    return this.artist.getGenres();
  }

  @Override
  public  Avatar[] getImages() {
    return this.avatars;
  }

  @Override
  public  String getUrl() {
    return this.url;
  }


  se.michaelthelin.spotify.model_objects.specification.Artist getArtist() {
    return this.artist;
  }
}
