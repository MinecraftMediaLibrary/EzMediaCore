/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.video.ScoreboardCallback.Builder;
import rewrite.dimension.Dimension;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;


public abstract sealed class VideoCallbackBuilder permits BlockHighlightCallback.Builder,
    ChatCallback.Builder, EntityCallback.Builder, MapCallback.Builder, Builder {

  private DelayConfiguration delay;
  private Dimension dims;
  private Viewers viewers;

  {
    this.delay = DelayConfiguration.DELAY_0_MS;
    this.viewers = Viewers.onlinePlayers();
  }

  @Contract(value = " -> new", pure = true)
  public static  BlockHighlightCallback.Builder blockHighlight() {
    return new BlockHighlightCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static  ChatCallback.Builder chat() {
    return new ChatCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static  <T extends Entity> EntityCallback.Builder<T> entity() {
    return new EntityCallback.Builder<>();
  }

  @Contract(value = " -> new", pure = true)
  public static  MapCallback.Builder map() {
    return new MapCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static  ScoreboardCallback.Builder scoreboard() {
    return new Builder();
  }

  @Contract("_ -> this")
  public  VideoCallbackBuilder delay( final DelayConfiguration delay) {
    this.delay = delay;
    return this;
  }

  @Contract("_ -> this")
  public  VideoCallbackBuilder dims( final Dimension dims) {
    this.dims = dims;
    return this;
  }

  @Contract("_ -> this")
  public  VideoCallbackBuilder viewers( final Viewers viewers) {
    this.viewers = viewers;
    return this;
  }

  public  DelayConfiguration getDelay() {
    return this.delay;
  }

  public  Dimension getDims() {
    return this.dims;
  }

  public  Viewers getViewers() {
    return this.viewers;
  }

  public abstract  FrameCallback build( final EzMediaCore core);
}
