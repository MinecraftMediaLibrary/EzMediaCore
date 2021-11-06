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
import io.github.pulsebeat02.ezmediacore.callback.ScoreboardCallback.Builder;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class CallbackBuilder {

  private DelayConfiguration delay;
  private Dimension dims;
  private Viewers viewers;

  {
    delay = DelayConfiguration.DELAY_0_MS;
    viewers = Viewers.onlinePlayers();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull BlockHighlightCallback.Builder blockHighlight() {
    return new BlockHighlightCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull ChatCallback.Builder chat() {
    return new ChatCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull EntityCallback.Builder entity() {
    return new EntityCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull MapCallback.Builder map() {
    return new MapCallback.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull ScoreboardCallback.Builder scoreboard() {
    return new Builder();
  }

  @Contract("_ -> this")
  public @NotNull CallbackBuilder delay(@NotNull final DelayConfiguration delay) {
    this.delay = delay;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull CallbackBuilder dims(@NotNull final Dimension dims) {
    this.dims = dims;
    return this;
  }

  @Contract("_ -> this")
  public @NotNull CallbackBuilder viewers(@NotNull final Viewers viewers) {
    this.viewers = viewers;
    return this;
  }

  public @NotNull DelayConfiguration getDelay() {
    return this.delay;
  }

  public @NotNull Dimension getDims() {
    return this.dims;
  }

  public @NotNull Viewers getViewers() {
    return this.viewers;
  }

  public abstract @NotNull FrameCallback build(@NotNull final MediaLibraryCore core);
}
