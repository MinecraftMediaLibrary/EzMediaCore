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
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatCallback extends FrameCallback implements ChatCallbackDispatcher {

  private final NamedEntityString character;

  ChatCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final Dimension dimension,
      @NotNull final NamedEntityString character,
      @NotNull final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    this.character = character;
  }

  @Override
  public void process(final int[] data) {
    final long time = System.currentTimeMillis();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      final Dimension dimension = this.getDimensions();
      this.getPacketHandler()
          .displayChat(
              this.getWatchers().getViewers(),
              this.character.getName(),
              data,
              dimension.getWidth(),
              dimension.getHeight());
    }
  }

  @Override
  public @NotNull NamedEntityString getChatCharacter() {
    return this.character;
  }

  public static final class Builder extends CallbackBuilder {

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;

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
    public @NotNull Builder character(@NotNull final NamedEntityString character) {
      this.character = character;
      return this;
    }

    @Override
    public @NotNull FrameCallback build(@NotNull final MediaLibraryCore core) {
      return new ChatCallback(
          core, this.getViewers(), this.getDims(), this.character, this.getDelay());
    }
  }
}
