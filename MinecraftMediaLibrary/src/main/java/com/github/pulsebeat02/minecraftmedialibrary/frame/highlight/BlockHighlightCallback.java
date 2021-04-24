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

package com.github.pulsebeat02.minecraftmedialibrary.frame.highlight;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.frame.FrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** The callback used to update block highlights for each frame when necessary. */
public final class BlockHighlightCallback implements FrameCallback {

  private final PacketHandler handler;
  private final UUID[] viewers;
  private final Location location;
  private final int videoWidth;
  private final int delay;
  private final int width;
  private final int height;
  private long lastUpdated;

  /**
   * Instantiates a new BlockHighlightCallback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param location the location
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   */
  public BlockHighlightCallback(
      @NotNull final MinecraftMediaLibrary library,
      final UUID[] viewers,
      @NotNull final Location location,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    handler = library.getHandler();
    this.viewers = viewers;
    this.location = location;
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
   * Sends the necessary data onto the clouds while dithering.
   *
   * @param data to send
   */
  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - lastUpdated >= delay) {
      lastUpdated = time;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          handler.displayDebugMarker(
              viewers,
              (int) (location.getX() - (width / 2D)) + x,
              (int) (location.getY() + (height / 2D)) - y,
              (int) location.getZ(),
              data[width * y + x],
              delay + 100);
        }
      }
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
   * Gets the PacketHandler.
   *
   * @return the library
   */
  public PacketHandler getHandler() {
    return handler;
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
   * Gets location.
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private Location location;
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
     * Sets the location.
     *
     * @param location the location
     * @return the location
     */
    public Builder setLocation(final Location location) {
      this.location = location;
      return this;
    }

    /**
     * Create entity cloud callback
     *
     * @param library the library
     * @return the entity cloud callback
     */
    public BlockHighlightCallback build(final MinecraftMediaLibrary library) {
      return new BlockHighlightCallback(
          library, viewers, location, width, height, videoWidth, delay);
    }
  }
}
