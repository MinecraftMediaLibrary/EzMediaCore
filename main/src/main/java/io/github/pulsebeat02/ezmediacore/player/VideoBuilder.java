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

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.JCodecMediaPlayer.Builder;
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedPlatformException;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class VideoBuilder {

  private Callback callback;
  private Dimension dims;
  private SoundKey key;
  private FrameConfiguration rate = FrameConfiguration.FPS_30;

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
    return new Builder();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull VideoBuilder unspecified() {
    return new VideoBuilder();
  }

  @Contract("_ -> this")
  public VideoBuilder callback(@NotNull final Callback callback) {
    this.callback = callback;
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
  public VideoBuilder soundKey(@NotNull final SoundKey key) {
    this.key = key;
    return this;
  }

  public void init() {
    Objects.requireNonNull(this.callback);
    this.calculateFrameRate();
  }

  public MediaPlayer build() {
    this.init();
    final MediaLibraryCore core = this.callback.getCore();
    switch (core.getDiagnostics().getSystem().getOSType()) {
      case WINDOWS, MAC -> {
        return this.vlcOption();
      }
      case UNIX -> {
        return core.isVLCSupported() ? this.vlcOption()
            : new FFmpegMediaPlayer(this.callback, this.callback.getWatchers(), this.dims,
                BufferConfiguration.BUFFER_10, this.key, this.rate);
      }
      default -> throw new UnsupportedPlatformException("Unknown");
    }
  }

  @Contract(" -> new")
  private @NotNull MediaPlayer vlcOption() {
    return vlc().callback(this.callback).dims(this.dims).soundKey(this.key).frameRate(this.rate)
        .build();
  }

  public void calculateFrameRate() {
    if (this.rate.getFps() == -1) {
      // this.rate = FrameConfiguration.ofFps(VideoFrameUtils.getFrameRate(this.callback.getCore(), Path.of(this.mrl.getMrl())).orElse(30.0));
      this.rate = FrameConfiguration.FPS_25;
    }
    this.dims = this.dims == null ? this.callback.getDimensions() : this.dims;
  }

  public Callback getCallback() {
    return this.callback;
  }

  public Dimension getDims() {
    return this.dims;
  }

  public SoundKey getKey() {
    return this.key;
  }

  public FrameConfiguration getRate() {
    return this.rate;
  }
}
