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
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import io.github.pulsebeat02.minecraftmedialibrary.frame.player.map.MapDataCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The callback used for itemframes to update maps for each frame when necessary.
 *
 * <p>You should pass in the necessary arguments that are needed. But also another argument which
 * specifies the dithering type. Each dithering type has it's own benefits. For example, Error
 * Diffusion Dithering algorithms (such as Floyd Steinberg, Filter Lite, etc) are known to provide
 * better and smoother color results rather than Ordered Dithering. However, the result is that it
 * is slower and will likely make the video player play less frames. Filter Lite is set by default
 * as it is very fast and provides very great results.
 */
public final class MapDataCallback extends Callback implements MapDataCallbackPrototype {

  private final PacketHandler handler;
  private final UUID[] viewers;
  private final DitherHolder type;
  private final int map;

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
      @NotNull final MediaLibrary library,
      final UUID[] viewers,
      @NotNull final DitherHolder type,
      final int map,
      final int width,
      final int height,
      final int videoWidth,
      final int delay) {
    super(library, width, height, videoWidth, delay);
    handler = library.getHandler();
    this.viewers = viewers;
    this.type = type;
    this.map = map;
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
    if (time - getLastUpdated() >= getDelay()) {
      setLastUpdated(time);
      final int width = getVideoWidth();
      handler.displayMaps(
          viewers, map, getWidth(), getHeight(), type.ditherIntoMinecraft(data, width), width);
    }
  }

  @Override
  public UUID[] getViewers() {
    return viewers;
  }

  @Override
  public long getMap() {
    return map;
  }

  @Override
  public PacketHandler getHandler() {
    return handler;
  }

  @Override
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
    public Builder viewers(@NotNull final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets map.
     *
     * @param map the map
     * @return the map
     */
    public Builder map(final int map) {
      this.map = map;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder itemframeWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder itemframeHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets video width.
     *
     * @param videoWidth the video width
     * @return the video width
     */
    public Builder videoWidth(final int videoWidth) {
      this.videoWidth = videoWidth;
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
     * Sets dither holder.
     *
     * @param holder the holder
     * @return the dither holder
     */
    public Builder ditherHolder(final DitherHolder holder) {
      type = holder;
      return this;
    }

    /**
     * Create item frame callback item frame callback.
     *
     * @param library the library
     * @return the item frame callback
     */
    public MapDataCallback build(final MediaLibrary library) {
      return new MapDataCallback(library, viewers, type, map, width, height, videoWidth, delay);
    }
  }
}
