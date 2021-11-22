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
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import org.jetbrains.annotations.NotNull;

public abstract class FrameCallback implements Callback {

  private final MediaLibraryCore core;
  private final DelayConfiguration delay;
  private final Dimension dimension;
  private final Viewers viewers;
  private long lastUpdated;

  public FrameCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final DelayConfiguration delay) {
    this.core = core;
    this.viewers = viewers;
    this.dimension = dimension;
    this.delay = delay;
  }

  @Override
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {}

  @Override
  public @NotNull DelayConfiguration getDelayConfiguration() {
    return this.delay;
  }

  @Override
  public long getLastUpdated() {
    return this.lastUpdated;
  }

  @Override
  public void setLastUpdated(final long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull PacketHandler getPacketHandler() {
    return this.core.getHandler();
  }

  @Override
  public @NotNull Viewers getWatchers() {
    return this.viewers;
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimension;
  }
}
