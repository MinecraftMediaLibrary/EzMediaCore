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

package com.github.pulsebeat02.minecraftmedialibrary.frame.map;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/** The callback used for itemframes to update maps for each frame when necessary. */
public final class MapDataCallback implements FrameCallback {

  private final MinecraftMediaLibrary library;
  private final UUID[] viewers;
  private final DitherHolder type;
  private final int map;
  private final int videoWidth;
  private final int delay;
  private final int width;
  private final int height;
  private long lastUpdated;

  /**
   * Instantiates a new Item frame callback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param map the map
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   * @param type the type
   */
  public MapDataCallback(
      @NotNull final MinecraftMediaLibrary library,
      final UUID[] viewers,
      @NotNull final DitherHolder type,
      final int map,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    this.library = library;
    this.viewers = viewers;
    this.type = type;
    this.map = map;
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sends the necessary data onto the itemframes while dithering.
   *
   * @param data to send
   */
  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - lastUpdated >= delay) {
      lastUpdated = time;
      final ByteBuffer dithered = type.ditherIntoMinecraft(data, videoWidth);
      library.getHandler().displayMaps(viewers, map, width, height, dithered, videoWidth);
    }
  }

  /**
   * Get viewers uuid [ ].
   *
   * @return the uuid [ ]
   */
  public UUID[] getViewers() {
    return viewers;
  }

  /**
   * Gets map.
   *
   * @return the map
   */
  public long getMap() {
    return map;
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
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets delay.
   *
   * @return the delay
   */
  public int getDelay() {
    return delay;
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
   * Gets video width.
   *
   * @return the video width
   */
  public int getVideoWidth() {
    return videoWidth;
  }

  /**
   * Gets last updated.
   *
   * @return the last updated
   */
  public long getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Gets the dithering type.
   *
   * @return the dithering type
   */
  public DitherHolder getType() {
    return type;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private DitherHolder type;
    private int map;
    private int width;
    private int height;
    private int videoWidth;
    private int delay;

    private Builder() {}

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder setViewers(@NotNull final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets map.
     *
     * @param map the map
     * @return the map
     */
    public Builder setMap(final int map) {
      this.map = map;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets video width.
     *
     * @param videoWidth the video width
     * @return the video width
     */
    public Builder setVideoWidth(final int videoWidth) {
      this.videoWidth = videoWidth;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder setDelay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Sets dither holder.
     *
     * @param holder the holder
     * @return the dither holder
     */
    public Builder setDitherHolder(final DitherHolder holder) {
      type = holder;
      return this;
    }

    /**
     * Create item frame callback item frame callback.
     *
     * @param library the library
     * @return the item frame callback
     */
    public MapDataCallback build(final MinecraftMediaLibrary library) {
      return new MapDataCallback(library, viewers, type, map, width, height, videoWidth, delay);
    }
  }
}
