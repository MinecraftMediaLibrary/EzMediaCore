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

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.AbstractVideoDetails;
import com.github.kiulian.downloader.model.playlist.PlaylistDetails;
import com.github.kiulian.downloader.model.playlist.YoutubePlaylist;
import io.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPlaylistException;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** A class for helping to get the videos from a specific Youtube playlist. */
public class YoutubePlaylistHelper implements YoutubePlaylistSearch {

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

  @Override
  @NotNull
  public List<String> getAlbumSongs() {
    final Optional<String> id = VideoExtractionUtilities.getYoutubeID(url);
    if (id.isPresent()) {
      try {
        final YoutubePlaylist playlist = new YoutubeDownloader().getPlaylist(id.get());
        details = playlist.details();
        return playlist.videos().stream()
            .map(AbstractVideoDetails::videoId)
            .collect(Collectors.toList());
      } catch (final YoutubeException e) {
        e.printStackTrace();
      }
    }
    throw new InvalidPlaylistException(String.format("Invalid Youtube Playlist! (%s)", url));
  }

  @Override
  public String getTitle() {
    return details.title();
  }

  @Override
  public String getAuthor() {
    return details.author();
  }

  @Override
  public int getVideoCount() {
    return details.videoCount();
  }

  @Override
  public int getViewCount() {
    return details.viewCount();
  }

  @Override
  public String getUrl() {
    return url;
  }
}
