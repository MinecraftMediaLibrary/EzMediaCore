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

package com.github.pulsebeat02.minecraftmedialibrary.playlist;

import com.github.pulsebeat02.minecraftmedialibrary.playlist.spotify.SpotifyPlaylistHelper;
import com.github.pulsebeat02.minecraftmedialibrary.playlist.youtube.YoutubePlaylistHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** A class used for audio playlist handling. */
public class AudioPlaylistControls implements PlaylistController {

  private final String url;
  private final List<String> songs;
  private final PlaylistType type;
  private int index;

  /**
   * Instantiate a new AudioPlaylist
   *
   * @param url the url
   * @param type the type of playlist
   */
  public AudioPlaylistControls(final String url, @NotNull final PlaylistType type) {
    this.url = url;
    songs = getSongs();
    this.type = type;
  }

  /**
   * Gets the song ids from the playlist.
   *
   * @return the song ids
   */
  private List<String> getSongs() {
    if (type == PlaylistType.YOUTUBE) {
      return new YoutubePlaylistHelper(url).getAlbumSongs();
    } else if (type == PlaylistType.SPOTIFY) {
      return new SpotifyPlaylistHelper(url).getAlbumSongs();
    } else {
      throw new UnsupportedOperationException("Unsupported Playlist!");
    }
  }

  @Override
  public synchronized String getCurrentSong() {
    return songs.get(index);
  }

  @Override
  public synchronized void skipForwardSong() {
    index++;
  }

  @Override
  public synchronized void skipBackwardSong() {
    index--;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public PlaylistType getType() {
    return type;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public synchronized void setIndex(final int index) {
    this.index = index;
  }
}
