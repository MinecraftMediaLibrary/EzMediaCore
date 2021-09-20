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
import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.DitherAlgorithmType;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.FloydDither;
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
    this.algorithm = algorithm;
    this.map = map.getValue();
    this.blockWidth = blockWidth;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    final Dimension dimension = this.getDimensions();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      final int width = this.blockWidth;
      this.getPacketHandler()
          .displayMaps(
              this.getWatchers().getViewers(),
              this.map,
              dimension.getWidth(),
              dimension.getHeight(),
              this.algorithm.ditherIntoMinecraft(data, width),
              width);
    }
  }

  @Override
  public long getMapId() {
    return this.map;
  }

  @Override
  public @NotNull DitherAlgorithm getAlgorithm() {
    return this.algorithm;
  }

  public static final class Builder extends CallbackBuilder {

    private DitherAlgorithm algorithm = new FloydDither();
    private Identifier<Integer> map = Identifier.ofIdentifier(0);
    private int blockWidth;

    Builder() {}

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
    public @NotNull Builder algorithm(@NotNull final DitherAlgorithm algorithm) {
      this.algorithm = algorithm;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder algorithm(@NotNull final DitherAlgorithmType algorithm) {
      this.algorithm = algorithm.getAlgorithm();
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
