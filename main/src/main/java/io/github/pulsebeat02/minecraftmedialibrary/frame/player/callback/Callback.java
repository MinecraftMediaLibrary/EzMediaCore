/*.........................................................................................
 . Copyright © 2021 Brandon Li
 .                                                                                        .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this
 . software and associated documentation files (the “Software”), to deal in the Software
 . without restriction, including without limitation the rights to use, copy, modify, merge,
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 . persons to whom the Software is furnished to do so, subject to the following conditions:
 .
 . The above copyright notice and this permission notice shall be included in all copies
 . or substantial portions of the Software.
 .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 . EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 . MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 . NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 . BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 . ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 . CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 .  SOFTWARE.
 .                                                                                        .
 .........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.player.callback;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.FrameCallbackAttribute;
import org.jetbrains.annotations.NotNull;

/** The abstract callback class. */
public abstract class Callback implements FrameCallbackAttribute {

  private final MediaLibrary library;
  private final int width;
  private final int height;
  private final int videoWidth;
  private final int delay;
  private long lastUpdated;

  /**
   * Initializes a new Callback.
   *
   * @param library the library
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public Callback(
      @NotNull final MediaLibrary library,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    Preconditions.checkArgument(width >= 0, "Width must be greater than or equal to 0!");
    Preconditions.checkArgument(height >= 0, "Height must be greater than or equal to 0!");
    Preconditions.checkArgument(
        delay >= 0, "Delay between frames must be greater than or equal to 0!");
    this.library = library;
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getDelay() {
    return delay;
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public int getVideoWidth() {
    return videoWidth;
  }

  @Override
  public long getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public void setLastUpdated(final long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
