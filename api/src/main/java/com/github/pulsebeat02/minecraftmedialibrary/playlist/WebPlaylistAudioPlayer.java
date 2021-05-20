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

import com.github.pulsebeat02.minecraftmedialibrary.annotation.LegacyApi;
import org.jetbrains.annotations.NotNull;

public class WebPlaylistAudioPlayer {

  private final AudioPlaylistControls playlist;

  /**
   * Instantiates a PlaylistAudioPlayer.
   *
   * @param url the url
   * @param type the playlist type
   */
  public WebPlaylistAudioPlayer(@NotNull final String url, @NotNull final PlaylistType type) {
    playlist = new AudioPlaylistControls(url, type);
  }

  /**
   * Instantiates a PlaylistAudioPlayer.
   *
   * @param url the url
   * @deprecated due to how it is better to specify the playlist type
   */
  @Deprecated
  @LegacyApi(since = "1.4.0")
  public WebPlaylistAudioPlayer(@NotNull final String url) {
    playlist =
        new AudioPlaylistControls(
            url, url.contains("open.spotify.com") ? PlaylistType.SPOTIFY : PlaylistType.YOUTUBE);
  }

  /** Skips forward a song. */
  public void skipForwardSong() {
    playlist.skipForwardSong();
    final String id = playlist.getCurrentSong();
    // .. play audio
  }

  /** Skips backward a song. */
  public void skipBackwardSong() {
    playlist.skipBackwardSong();
    final String id = playlist.getCurrentSong();
    // .. play audio
  }

  /** Pauses the audio. */
  public void pauseSong() {
    // .. pause audio
  }

  /** Resumes the audio. */
  public void resumeSong() {
    // .. resume audio
  }
}
