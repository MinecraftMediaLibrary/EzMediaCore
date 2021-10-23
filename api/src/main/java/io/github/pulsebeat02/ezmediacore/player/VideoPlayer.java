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
package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.Viewable;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public interface VideoPlayer extends LibraryInjectable, Viewable, Dimensional {

  void initializePlayer(final long ms, @NotNull final Object... arguments);

  void playAudio();

  void stopAudio();

  void setCustomAudioPlayback(@NotNull final Consumer<MrlConfiguration> runnable);

  void setCustomAudioStopper(@NotNull final Runnable runnable);

  void setPlayerState(@NotNull final PlayerControls controls, @NotNull final Object... arguments);

  void setDimensions(@NotNull final Dimension dimensions);

  void setViewers(@NotNull final Viewers viewers);

  void onPlayerStateChange(
      @NotNull final PlayerControls status, @NotNull final Object... arguments);

  @NotNull
  Callback getCallback();

  void setCallback(@NotNull final Callback callback);

  @NotNull
  MrlConfiguration getDirectVideoMrl();

  void setDirectVideoMrl(@NotNull final MrlConfiguration configuration);

  @NotNull
  MrlConfiguration getDirectAudioMrl();

  void setDirectAudioMrl(@NotNull final MrlConfiguration configuration);

  @NotNull
  FrameConfiguration getFrameConfiguration();

  void setFrameConfiguration(@NotNull final FrameConfiguration configuration);

  @NotNull
  SoundKey getSoundKey();

  void setSoundKey(@NotNull final SoundKey key);

  @NotNull
  PlayerControls getPlayerState();

  long getElapsedMilliseconds();

  @NotNull
  PlayerType getPlayerType();

  boolean isBuffered();
}
