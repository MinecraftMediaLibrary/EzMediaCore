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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedEntityString;
import io.github.pulsebeat02.ezmediacore.callback.implementation.ChatCallbackDispatcher;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.nio.IntBuffer;
import java.util.UUID;
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
    checkNotNull(character, "Entity name cannot be null!");
    this.character = character;
  }

  @Override
  public void process(final int @NotNull [] data) {
    final long time = System.currentTimeMillis();
    final UUID[] viewers = this.getWatchers().getViewers();
    final Dimension dimension = this.getDimensions();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.displayChat(viewers, dimension, IntBuffer.wrap(data));
    }
  }

  private void displayChat(
      @NotNull final UUID[] viewers,
      @NotNull final Dimension dimension,
      @NotNull final IntBuffer data) {
    this.getPacketHandler()
        .displayChat(
            this.getWatchers().getViewers(),
            data,
            this.character.getName(),
            dimension.getWidth(),
            dimension.getHeight());
  }

  @Override
  public @NotNull NamedEntityString getChatCharacter() {
    return this.character;
  }

  public static final class Builder extends CallbackBuilder {

    private NamedEntityString character = NamedEntityString.NORMAL_SQUARE;

    public Builder() {}

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
