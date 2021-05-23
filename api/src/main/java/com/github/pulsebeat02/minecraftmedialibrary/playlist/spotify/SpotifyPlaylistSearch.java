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

import org.jetbrains.annotations.NotNull;

import java.util.List;

/** A Spotify playlist searcher class. */
public interface SpotifyPlaylistSearch {

  /**
   * Gets all the album songs of the playlist.
   *
   * @return the album songs
   */
  @NotNull
  List<String> getAlbumSongs();

  /**
   * Gets the title of the playlist.
   *
   * @return the title
   */
  String getTitle();

  /**
   * Gets the displayed name of the author.
   *
   * @return the author
   */
  String getAuthor();

  /**
   * Gets the video count of the playlist
   *
   * @return the video count
   */
  int getVideoCount();

  /**
   * Gets the follower count of the playlist.
   *
   * @return the follower count
   */
  int getFollowerCount();

  /**
   * Gets the description of the playlist.
   *
   * @return the description
   */
  String getDescription();

  /**
   * Gets the URL of the playlist.
   *
   * @return the url
   */
  String getUrl();
}
