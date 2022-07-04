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
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewable;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.PlayerInput;
import io.github.pulsebeat02.ezmediacore.player.output.PlayerOutput;
import org.jetbrains.annotations.NotNull;

public interface VideoPlayer extends LibraryInjectable, Viewable, Dimensional {

  @NotNull VideoCallback getVideoCallback();

  @NotNull AudioCallback getAudioCallback();

  void start(@NotNull final Input mrl, @NotNull final Object... arguments);

  void pause();

  void resume();

  void release();

  void setViewers(@NotNull final Viewers viewers);

  void onPlayerStateChange(
      @NotNull final Input mrl,
      @NotNull final PlayerControls controls,
      @NotNull final Object... arguments);

  @NotNull PlayerInput getInput();

  void setInput(@NotNull final PlayerInput input);

  @NotNull PlayerOutput<?> getOutput();

  void setOutput(@NotNull final PlayerOutput<?> output);

  @NotNull
  PlayerControls getPlayerState();
}
