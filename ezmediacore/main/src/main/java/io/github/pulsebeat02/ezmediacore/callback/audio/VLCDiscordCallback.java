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
package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/*
 * VLCDiscordCallback assumes the use for a Discord bot. While it does
 * not directly support actual libraries for Discord bots (for example, JDA
 * or Discord4J) it does allow you to pass in a consumer which will be
 * sent data from VLC for the library to input. The data is in a byte
 * array and wav format.
 */
public final class VLCDiscordCallback extends DiscordCallback {

  private final Consumer<byte[]> audioConsumer;

  VLCDiscordCallback(
      @NotNull final MediaLibraryCore core, @NotNull final Consumer<byte[]> audioConsumer) {
    super(core);
    this.audioConsumer = audioConsumer;
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {}

  @Override
  public void process(final byte @NotNull [] data) {
    this.audioConsumer.accept(data);
  }

  public static final class Builder extends AudioOutputBuilder {

    private Consumer<byte[]> consumer;

    @Contract("_ -> this")
    public @NotNull Builder consumer(@NotNull final Consumer<byte[]> consumer) {
      this.consumer = consumer;
      return this;
    }

    @Contract("_ -> new")
    @Override
    public @NotNull AudioOutput build(@NotNull final MediaLibraryCore core) {
      checkNotNull(this.consumer, "Discord audio consumer cannot be null!");
      return new VLCDiscordCallback(core, this.consumer);
    }
  }
}
