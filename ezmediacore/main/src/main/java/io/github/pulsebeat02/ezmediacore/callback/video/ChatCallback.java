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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.entity.NamedStringCharacter;
import io.github.pulsebeat02.ezmediacore.callback.implementation.ChatCallbackDispatcher;
import rewrite.dimension.Dimension;
import java.nio.IntBuffer;
import java.util.UUID;
import org.jetbrains.annotations.Contract;


public class ChatCallback extends FrameCallback implements ChatCallbackDispatcher {

  private final NamedStringCharacter character;

  ChatCallback(
       final EzMediaCore core,
       final Viewers viewers,
       final Dimension dimension,
       final NamedStringCharacter character,
       final DelayConfiguration delay) {
    super(core, viewers, dimension, delay);
    checkNotNull(character, "Entity name cannot be null!");
    this.character = character;
  }

  @Override
  public void process(final int  [] data) {
    final long time = System.currentTimeMillis();
    final UUID[] viewers = this.getWatchers().getViewers();
    final Dimension dimension = this.getDimensions();
    if (time - this.getLastUpdated() >= this.getDelayConfiguration().getDelay()) {
      this.setLastUpdated(time);
      this.displayChat(viewers, dimension, IntBuffer.wrap(data));
    }
  }

  private void displayChat(
       final UUID[] viewers,
       final Dimension dimension,
       final IntBuffer data) {
    final UUID[] watchers = this.getWatchers().getViewers();
    final String character = this.character.getCharacter();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();
    this.getPacketHandler().displayChat(watchers, data, character, width, height);
  }

  @Override
  public  NamedStringCharacter getChatCharacter() {
    return this.character;
  }

  public static final class Builder extends VideoCallbackBuilder {

    private NamedStringCharacter character = NamedStringCharacter.NORMAL_SQUARE;

    public Builder() {}

    @Contract("_ -> this")
    @Override
    public  Builder delay( final DelayConfiguration delay) {
      super.delay(delay);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public  Builder dims( final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public  Builder viewers( final Viewers viewers) {
      super.viewers(viewers);
      return this;
    }

    @Contract("_ -> this")
    public  Builder character( final NamedStringCharacter character) {
      this.character = character;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public  FrameCallback build( final EzMediaCore core) {
      return new ChatCallback(
          core, this.getViewers(), this.getDims(), this.character, this.getDelay());
    }
  }
}
