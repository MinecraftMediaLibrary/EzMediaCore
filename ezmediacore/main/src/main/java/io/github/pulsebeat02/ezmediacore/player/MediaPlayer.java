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

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioOutput;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.jlibdl.component.Format;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.player.input.PlayerInput;
import io.github.pulsebeat02.ezmediacore.player.input.PlayerInputHolder;
import io.github.pulsebeat02.ezmediacore.player.input.implementation.UrlInput;
import io.github.pulsebeat02.ezmediacore.player.output.PlayerOutput;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MediaPlayer implements VideoPlayer {

  private final MediaLibraryCore core;
  private final Dimension dimensions;
  private final InputParser parser;
  private final VideoCallback video;
  private final AudioSource audio;

  private Viewers viewers;
  private PlayerInput input;
  private PlayerOutput output;
  private PlayerControls controls;

  public MediaPlayer(
      @NotNull final VideoCallback video,
      @NotNull final AudioSource audio,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final InputParser parser) {
    checkNotNull(video, "Video callback cannot be null!");
    checkNotNull(video, "Audio callback cannot be null!");
    checkNotNull(viewers, "Viewers cannot be null!");
    checkNotNull(pixelDimension, "Pixel dimension cannot be null!");
    this.core = video.getCore();
    this.video = video;
    this.audio = audio;
    this.dimensions = pixelDimension;
    this.viewers = viewers;
    this.parser = parser;
  }

  @Override
  public @NotNull VideoCallback getVideoCallback() {
    return this.video;
  }

  @Override
  public @NotNull PlayerControls getPlayerState() {
    return this.controls;
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    checkNotNull(mrl, "MRL cannot be null!");
    final MediaRequest request = RequestUtils.requestMediaInformation(mrl);
    final Format video = request.getVideoLinks().get(0);
    final Format audio = request.getAudioLinks().get(0);
    final Input videoInput = UrlInput.ofUrl(video.getUrl());
    final Input audioInput = UrlInput.ofUrl(audio.getUrl());
    this.input = PlayerInputHolder.ofInputs(videoInput, audioInput);
    this.controls = PlayerControls.START;
    this.onPlayerStateChange(mrl, this.controls, arguments);
    this.core
        .getLogger()
        .info(Locale.PLAYER_START.build(mrl.getInput(), Arrays.toString(arguments)));
  }

  @Override
  public void pause() {
    this.controls = PlayerControls.PAUSE;
    this.onPlayerStateChange(null, this.controls);
    this.core.getLogger().info(Locale.PLAYER_PAUSE.build());
  }

  @Override
  public void resume() {
    final Input mrl = this.getInput().getDirectVideoMrl();
    checkNotNull(mrl, "Input cannot be null!");
    this.controls = PlayerControls.RESUME;
    this.onPlayerStateChange(mrl, this.controls);
    this.core.getLogger().info(Locale.PLAYER_RESUME.build());
  }

  @Override
  public void release() {
    this.controls = PlayerControls.RELEASE;
    this.onPlayerStateChange(null, this.controls);
    this.core.getLogger().info(Locale.PLAYER_RELEASE.build());
  }

  @Override
  public void onPlayerStateChange(
      @Nullable final Input mrl,
      @NotNull final PlayerControls controls,
      @NotNull final Object... arguments) {
    this.video.preparePlayerStateChange(this, controls);
    this.audio.preparePlayerStateChange(this, controls);
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimensions;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull Viewers getWatchers() {
    return this.viewers;
  }

  @Override
  public @NotNull AudioSource getAudioCallback() {
    return this.audio;
  }

  @Override
  public void setViewers(@NotNull final Viewers viewers) {
    this.viewers = viewers;
  }

  @Override
  public @NotNull PlayerInput getInput() {
    return this.input;
  }

  @Override
  public void setInput(@NotNull final PlayerInput input) {
    this.input = input;
  }

  @Override
  public @NotNull PlayerOutput getOutput() {
    return this.output;
  }

  @Override
  public void setOutput(@NotNull final PlayerOutput output) {
    this.output = output;
  }

  public @NotNull InputParser getInputParser() {
    return this.parser;
  }
}
