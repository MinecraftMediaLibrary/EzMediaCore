/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

package com.github.pulsebeat02.minecraftmedialibrary.video.player;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class VideoPlayerBase {

  private final MinecraftMediaLibrary library;
  private final String url;
  private final Consumer<int[]> callback;
  private int width;
  private int height;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VideoPlayerBase(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    this.library = library;
    this.url = url;
    this.width = width;
    this.height = height;
    this.callback = callback;
  }

  /**
   * Gets library.
   *
   * @return the library
   */
  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(final int width) {
    this.width = width;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(final int height) {
    this.height = height;
  }

  /**
   * Gets callback.
   *
   * @return the callback
   */
  public Consumer<int[]> getCallback() {
    return callback;
  }

  /** Starts player. */
  public abstract void start();

  /** Stops player. */
  public abstract void stop();
}
