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

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioOutput;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.dimension.FrameDimension;
import io.github.pulsebeat02.ezmediacore.player.buffered.BufferConfiguration;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.JCodecMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedPlatformException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class VideoBuilder {

  private VideoCallback video;
  private Dimension dims = FrameDimension.X5_5;
  private FrameConfiguration rate = FrameConfiguration.FPS_30;

  private AudioOutput audio;

  @Contract(value = " -> new", pure = true)
  public static @NotNull VLCMediaPlayer.Builder vlc() {
    return new VLCMediaPlayer.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull FFmpegMediaPlayer.Builder ffmpeg() {
    return new FFmpegMediaPlayer.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull JCodecMediaPlayer.Builder jcodec() {
    return new JCodecMediaPlayer.Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull VideoBuilder unspecified() {
    return new VideoBuilder();
  }

  @Contract("_ -> this")
  public VideoBuilder video(@NotNull final VideoCallback callback) {
    this.video = callback;
    return this;
  }

  @Contract("_ -> this")
  public VideoBuilder frameRate(@NotNull final FrameConfiguration rate) {
    this.rate = rate;
    return this;
  }

  @Contract("_ -> this")
  public VideoBuilder dims(@NotNull final Dimension dims) {
    this.dims = dims;
    return this;
  }

  @Contract("_ -> this")
  public VideoBuilder audio(@NotNull final AudioOutput audio) {
    this.audio = audio;
    return this;
  }

  public void init() {
    requireNonNull(this.video);
    this.calculateFrameRate();
  }

  public MediaPlayer build() {
    this.init();
    final MediaLibraryCore core = this.video.getCore();
    switch (core.getDiagnostics().getSystem().getOSType()) {
      case WINDOWS, MAC -> {
        return this.vlcOption();
      }
      case UNIX -> {
        return core.isVLCSupported() ? this.vlcOption() : this.createFFmpegMediaPlayer();
      }
      default -> throw new UnsupportedPlatformException("Unknown");
    }
  }

  @NotNull
  private MediaPlayer createFFmpegMediaPlayer() {
    return new FFmpegMediaPlayer.Builder()
        .video(this.video)
        .audio(this.audio)
        .dims(this.dims)
        .buffer(BufferConfiguration.BUFFER_10)
        .frameRate(this.rate)
        .build();
  }

  @Contract(" -> new")
  private @NotNull MediaPlayer vlcOption() {
    return vlc()
        .video(this.video)
        .audio(this.audio)
        .dims(this.dims)
        .frameRate(this.rate)
        .audio(this.audio)
        .build();
  }

  public void calculateFrameRate() {
    if (this.rate.getFps() == -1) {
      // this.rate = FrameConfiguration.ofFps(VideoFrameUtils.getFrameRate(this.callback.getCore(),
      // Path.of(this.mrl.getMrl())).orElse(30.0));
      this.rate = FrameConfiguration.FPS_25;
    }
    this.dims = this.dims == null ? this.video.getDimensions() : this.dims;
  }

  public VideoCallback getVideo() {
    return this.video;
  }

  public AudioSource getAudio() {
    return this.audio;
  }

  public Dimension getDims() {
    return this.dims;
  }

  public FrameConfiguration getRate() {
    return this.rate;
  }
}
