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

package com.github.pulsebeat02.minecraftmedialibrary.frame;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/** A context class used to provide for video players. */
public interface VideoPlayerContext {

  /**
   * Gets library.
   *
   * @return the library
   */
  MediaLibrary getLibrary();

  /**
   * Gets url.
   *
   * @return the url
   */
  String getUrl();

  /**
   * Gets width.
   *
   * @return the width
   */
  int getWidth();

  /**
   * Sets width.
   *
   * @param width the width
   */
  void setWidth(int width);

  /**
   * Gets height.
   *
   * @return the height
   */
  int getHeight();

  /**
   * Sets height.
   *
   * @param height the height
   */
  void setHeight(int height);

  /**
   * Gets callback.
   *
   * @return the callback
   */
  FrameCallback getCallback();

  /**
   * Gets the sound name for the resourcepack.
   *
   * @return the sound name
   */
  String getSound();

  /**
   * Starts the player.
   *
   * @param players which players to play the audio for
   */
  void start(@NotNull Collection<? extends Player> players);

  /**
   * Stops the player.
   *
   * @param players the players
   */
  void stop(@NotNull Collection<? extends Player> players);

  /** Releases the player. */
  void release();

  /**
   * Repeats the player.
   *
   * @param setting the setting
   */
  void setRepeat(boolean setting);

  /**
   * Returns whether the video is playing.
   *
   * @return whether the video is playing or not
   */
  boolean isPlaying();
}
