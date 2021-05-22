/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.playlist.spotify;

import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPlaylistException;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** A class for helping to get the videos from a specific Spotify playlist. */
public class SpotifyPlaylistHelper implements SpotifyPlaylistHelperBase {

  private static final SpotifyApi SPOTIFY_API;

  static {
    SPOTIFY_API = new SpotifyApi.Builder().setAccessToken("").build();
  }

  private final String url;
  private Playlist playlist;

  /**
   * Instantiates a new SpotifyPlaylistHelper
   *
   * @param url the url of the playlist
   */
  public SpotifyPlaylistHelper(@NotNull final String url) {
    this.url = url;
  }

  @Override
  @NotNull
  public List<String> getAlbumSongs() {
    final Optional<String> id = VideoExtractionUtilities.getSpotifyID(url);
    if (id.isPresent()) {
      try {
        playlist = SPOTIFY_API.getPlaylist(id.get()).build().execute();
        return Arrays.stream(playlist.getTracks().getItems())
            .map(x -> x.getTrack().getId())
            .collect(Collectors.toList());
      } catch (final IOException | ParseException | SpotifyWebApiException e) {
        e.printStackTrace();
      }
    }
    throw new InvalidPlaylistException(String.format("Invalid Spotify Playlist! (%s)", url));
  }

  @Override
  public String getTitle() {
    return playlist.getName();
  }

  @Override
  public String getAuthor() {
    return playlist.getOwner().getDisplayName();
  }

  @Override
  public int getVideoCount() {
    return playlist.getTracks().getItems().length;
  }

  @Override
  public int getFollowerCount() {
    return playlist.getFollowers().getTotal();
  }

  @Override
  public String getDescription() {
    return playlist.getDescription();
  }

  @Override
  public String getUrl() {
    return url;
  }
}
