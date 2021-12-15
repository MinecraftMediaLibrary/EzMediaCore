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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.utility.media.MediaExtractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

public class SpotifyPlaylist implements TrackPlaylist {

  private final Playlist playlist;
  private final String url;
  private final List<PlaylistTrack> tracks;

  SpotifyPlaylist(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    checkNotNull(url, "URL cannot be null!");
    this.url = url;
    this.playlist = this.getInternalPlaylist();
    this.tracks = this.getInternalTracks();
  }

  @Contract("_ -> new")
  public static @NotNull SpotifyPlaylist ofSpotifyPlaylist(@NotNull final String url)
      throws IOException, ParseException, SpotifyWebApiException {
    return new SpotifyPlaylist(url);
  }

  private @NotNull List<PlaylistTrack> getInternalTracks() {
    return Arrays.stream(this.playlist.getTracks().getItems())
        .map(SpotifyPlaylistTrack::new)
        .collect(Collectors.toList());
  }

  private @NotNull Playlist getInternalPlaylist()
      throws IOException, ParseException, SpotifyWebApiException {
    return SpotifyProvider.getSpotifyApi()
        .getPlaylist(MediaExtractionUtils.getSpotifyIDExceptionally(this.url))
        .build()
        .execute();
  }

  @Override
  public @NotNull SpotifyUser getAuthor() {
    return new SpotifyUser(this.playlist.getOwner());
  }

  @Override
  public int getVideoCount() {
    return this.playlist.getTracks().getTotal();
  }

  @Override
  public @NotNull String getUrl() {
    return this.url;
  }

  @Override
  public @NotNull String getName() {
    return this.playlist.getName();
  }

  @Override
  public @NotNull List<PlaylistTrack> getTracks() {
    return this.tracks;
  }

  @Override
  public @NotNull String getDescription() {
    return this.playlist.getDescription();
  }

  @Override
  public int getTotalFollowers() {
    return this.playlist.getFollowers().getTotal();
  }

  @Override
  public boolean isCollaborative() {
    return this.playlist.getIsCollaborative();
  }

  @Override
  public boolean isPubliclyAccessible() {
    return this.playlist.getIsPublicAccess();
  }

  @Override
  public @NotNull Map<String, String> getExternalUrls() {
    return this.playlist.getExternalUrls().getExternalUrls();
  }

  @NotNull
  Playlist getPlaylist() {
    return this.playlist;
  }
}
