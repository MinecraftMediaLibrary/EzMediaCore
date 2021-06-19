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

package io.github.pulsebeat02.minecraftmedialibrary.frame.player;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.FrameCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.VideoPlayerContext;
import org.jetbrains.annotations.NotNull;

/** The abstract video player class. */
public abstract class VideoPlayer implements VideoPlayerContext {

  private final MediaLibrary library;
  private final FrameCallback callback;
  private final String sound;
  private final String url;
  private int width;
  private int height;
  private boolean playing;
  private boolean repeat;
  private long start;
  private int frameRate;

  /**
   * Initializes a new VideoPlayer.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VideoPlayer(
      @NotNull final MediaLibrary library,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final FrameCallback callback) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be empty or null!");
    Preconditions.checkArgument(width > 0, String.format("Width is not valid! (%d)", width));
    Preconditions.checkArgument(height > 0, String.format("Height is not valid! (%d)", height));
    this.library = library;
    this.url = url;
    this.width = width;
    this.height = height;
    this.callback = callback;
    frameRate = 25;
    sound = getLibrary().getPlugin().getName().toLowerCase();
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public void setWidth(final int width) {
    this.width = width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void setHeight(final int height) {
    this.height = height;
  }

  @Override
  public FrameCallback getCallback() {
    return callback;
  }

  @Override
  public String getSound() {
    return sound;
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }

  @Override
  public void setPlaying(final boolean playing) {
    this.playing = playing;
  }

  @Override
  public int getFrameRate() {
    return frameRate;
  }

  @Override
  public void setFrameRate(final int frameRate) {
    this.frameRate = frameRate;
  }

  @Override
  public boolean isRepeat() {
    return repeat;
  }

  @Override
  public void setRepeat(final boolean repeat) {
    this.repeat = repeat;
  }

  @Override
  public long getStart() {
    return start;
  }

  @Override
  public void setStart(final long start) {
    this.start = start;
  }
}
