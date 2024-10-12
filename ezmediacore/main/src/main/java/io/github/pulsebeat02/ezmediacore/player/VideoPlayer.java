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
package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.PlayerInput;
import io.github.pulsebeat02.ezmediacore.player.output.PlayerOutput;


public interface VideoPlayer , Viewable, Dimensional {


  VideoCallback getVideoCallback();


  AudioSource getAudioCallback();

  void start( final Input mrl,  final Object... arguments);

  void pause();

  void resume();

  void release();

  void setViewers( final Viewers viewers);

  void onPlayerStateChange(
       final Input mrl,
       final PlayerControls controls,
       final Object... arguments);


  PlayerInput getInput();

  void setInput( final PlayerInput input);


  PlayerOutput getOutput();

  void setOutput( final PlayerOutput output);


  PlayerControls getPlayerState();
}
