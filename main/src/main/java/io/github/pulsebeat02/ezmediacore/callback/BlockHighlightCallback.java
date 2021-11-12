/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BlockHighlightCallback extends FrameCallback
    implements BlockHighlightCallbackDispatcher {

  private final Location location;

  BlockHighlightCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final Location location,
      @NotNull final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    this.location = location;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final long delay = this.getDelayConfiguration().getDelay();
    if (time - this.getLastUpdated() >= delay) {
      this.setLastUpdated(time);
      final Dimension dimension = this.getDimensions();
      final int width = dimension.getWidth();
      final int height = dimension.getHeight();
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          this.getPacketHandler()
              .displayDebugMarker(
                  this.getWatchers().getViewers(),
                  (int) (this.location.getX() - (width / 2D)) + x,
                  (int) (this.location.getY() + (height / 2D)) - y,
                  (int) this.location.getZ(),
                  data[width * y + x],
                  (int) (delay + 100));
        }
      }
    }
  }

  @Override
  public @NotNull Location getLocation() {
    return this.location;
  }

  public static final class Builder extends CallbackBuilder {

    private Location location;

    Builder() {
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder delay(@NotNull final DelayConfiguration delay) {
      super.delay(delay);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public @NotNull Builder viewers(@NotNull final Viewers viewers) {
      super.viewers(viewers);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder location(@NotNull final Location location) {
      this.location = location;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new BlockHighlightCallback(
          core, this.getViewers(), this.getDims(), this.location, this.getDelay());
    }
  }
}
