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

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.highlight.BlockHighlightCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The callback used to update block highlights for each frame when necessary.
 *
 * <p>This video player is only visible if the player has debug mode turned on. It uses custom
 * packets to send to the player with the RGB colors to make a video player.
 */
public final class BlockHighlightCallback extends Callback
    implements BlockHighlightCallbackPrototype {

  private final PacketHandler handler;
  private final UUID[] viewers;
  private final Location location;

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
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      @NotNull final Location location,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    super(library, width, height, videoWidth, delay);
    handler = library.getHandler();
    this.viewers = viewers;
    this.location = location;
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
    final int delay = getDelay();
    if (time - getLastUpdated() >= delay) {
      setLastUpdated(time);
      final int height = getHeight();
      final int width = getWidth();
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

  @Override
  public UUID[] getViewers() {
    return viewers;
  }

  @Override
  public PacketHandler getHandler() {
    return handler;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  /** The type Builder. */
  public static class Builder {

    private UUID[] viewers;
    private Location location;
    private int width;
    private int height;
    private int delay;

    private Builder() {}

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder viewers(@NotNull final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder highlightWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder highlightHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder delay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the location
     * @return the location
     */
    public Builder location(final Location location) {
      this.location = location;
      return this;
    }

    /**
     * Create entity cloud callback
     *
     * @param library the library
     * @return the entity cloud callback
     */
    public BlockHighlightCallbackPrototype build(final MediaLibrary library) {
      return new BlockHighlightCallback(library, viewers, location, width, height, width, delay);
    }
  }
}
