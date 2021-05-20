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

package com.github.pulsebeat02.minecraftmedialibrary.playlist.youtube;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.playlist.PlaylistDetails;
import com.github.kiulian.downloader.model.playlist.PlaylistVideoDetails;
import com.github.kiulian.downloader.model.playlist.YoutubePlaylist;
import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPlaylistException;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/** A class for helping to get the videos from a specific Youtube playlist. */
public class YoutubePlaylistHelper {

  private final String url;
  private PlaylistDetails details;

  /**
   * Instantiates a new YoutubePlaylistHelper
   *
   * @param url the url of the playlist
   */
  public YoutubePlaylistHelper(@NotNull final String url) {
    this.url = url;
  }

  /**
   * Gets all the video ids for the playlist.
   *
   * @return the video ids of the playlist
   */
  @NotNull
  public List<PlaylistVideoDetails> getAlbumSongs() {
    final Optional<String> id = VideoExtractionUtilities.getYoutubeID(url);
    if (id.isPresent()) {
      try {
        final YoutubePlaylist playlist = new YoutubeDownloader().getPlaylist(id.get());
        details = playlist.details();
        return playlist.videos();
      } catch (final YoutubeException e) {
        e.printStackTrace();
      }
    }
    throw new InvalidPlaylistException(String.format("Invalid Youtube Playlist! (%s)", url));
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return details.title();
  }

  /**
   * Gets the author.
   *
   * @return the author
   */
  public String getAuthor() {
    return details.author();
  }

  /**
   * Gets the video count.
   *
   * @return the video count
   */
  public int getVideoCount() {
    return details.videoCount();
  }

  /**
   * Gets the view count.
   *
   * @return the view count
   */
  public int getViewCount() {
    return details.viewCount();
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the playlist details.
   *
   * @return the playlist details
   */
  public PlaylistDetails getDetails() {
    return details;
  }
}
