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
package io.github.pulsebeat02.ezmediacore.callback.video;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.callback.implementation.MapCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.error.FilterLiteDither;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MapCallback extends FrameCallback implements MapCallbackDispatcher {

  private final DitherAlgorithm algorithm;
  private final int map;
  private final int blockWidth;

  MapCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final DitherAlgorithm algorithm,
      @NotNull final Identifier<Integer> map,
      @NotNull final DelayConfiguration delay,
      final int blockWidth) {
    super(core, viewers, dimension, delay);
    checkArgument(map.getValue() >= 0, "Map id must be greater than or equal to 0!");
    checkNotNull(algorithm, "Algorithm cannot be null!");
    this.algorithm = algorithm;
    this.map = map.getValue();
    this.blockWidth = blockWidth;
  }

  @Override
  public void process(final int @NotNull [] data) {
    final long time = System.currentTimeMillis();
    final Dimension dimension = this.getDimensions();
    final UUID[] viewers = this.getWatchers().getViewers();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.displayMaps(viewers, dimension, data);
    }
  }

  private void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final Dimension dimension,
      final int @NotNull [] data) {
    final int blockWidth = this.blockWidth;
    final BufferCarrier dithered = this.algorithm.ditherIntoMinecraft(data, blockWidth);
    final int height = dimension.getHeight();
    final int width = dimension.getWidth();
    this.getPacketHandler().displayMaps(viewers, dithered, this.map, width, height, blockWidth);
  }

  @Override
  public long getMapId() {
    return this.map;
  }

  @Override
  public @NotNull DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }

  public static final class Builder extends VideoCallbackBuilder {

    private DitherAlgorithm algorithm = new FilterLiteDither();
    private Identifier<Integer> map = Identifier.ofIdentifier(0);
    private int blockWidth;

    public Builder() {}

    @Contract("_ -> this")
    public @NotNull Builder algorithm(@NotNull final DitherAlgorithm algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder map(@NotNull final Identifier<Integer> id) {
      this.map = id;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder blockWidth(final int blockWidth) {
      this.blockWidth = blockWidth;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new MapCallback(
          core,
          this.getViewers(),
          this.getDims(),
          this.algorithm,
          this.map,
          this.getDelay(),
          this.blockWidth);
    }
  }
}
