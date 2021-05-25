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

package io.github.pulsebeat02.minecraftmedialibrary.playlist.youtube;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface YoutubePlaylistSearch {

  /**
   * Gets all the video ids for the playlist.
   *
   * @return the video ids of the playlist
   */
  @NotNull
  List<String> getAlbumSongs();

  /**
   * Gets the title.
   *
   * @return the title
   */
  String getTitle();

  /**
   * Gets the author.
   *
   * @return the author
   */
  String getAuthor();

  /**
   * Gets the video count.
   *
   * @return the video count
   */
  int getVideoCount();

  /**
   * Gets the view count.
   *
   * @return the view count
   */
  int getViewCount();

  /**
   * Gets the url.
   *
   * @return the url
   */
  String getUrl();
}
