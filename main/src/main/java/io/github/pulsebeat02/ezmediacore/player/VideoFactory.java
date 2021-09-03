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

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.throwable.UnsupportedPlatformException;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class VideoFactory {

  private Callback callback;
  private Dimension dims;
  private String url;
  private String key;
  private int rate = 30;

  @Contract(value = " -> new", pure = true)
  public static @NotNull VLCMediaFactory vlc() {
    return new VLCMediaFactory();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull FFmpegMediaFactory ffmpeg() {
    return new FFmpegMediaFactory();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull JCodecMediaFactory jcodec() {
    return new JCodecMediaFactory();
  }

  @Contract(value = " -> new", pure = true)
  public static @NotNull VideoFactory unspecified() {
    return new VideoFactory();
  }

  public VideoFactory callback(@NotNull final Callback callback) {
    this.callback = callback;
    return this;
  }

  public VideoFactory url(@NotNull final String url) {
    this.url = url;
    return this;
  }

  // set to -1 for default
  public VideoFactory frameRate(final int rate) {
    this.rate = rate;
    return this;
  }

  public VideoFactory dims(@NotNull final Dimension dims) {
    this.dims = dims;
    return this;
  }

  public VideoFactory soundKey(@NotNull final String key) {
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
            : new FFmpegMediaPlayer(this.callback, this.dims, 10, this.url, this.key, this.rate);
      }
      default -> throw new UnsupportedPlatformException("Unknown");
    }
  }

  @Contract(" -> new")
  private @NotNull MediaPlayer vlcOption() {
    return new VLCMediaPlayer(this.callback, this.dims, this.url, this.key, this.rate);
  }

  public void calculateFrameRate() {
    if (this.rate == -1) {
      try {
        this.rate = (int) VideoFrameUtils.getFrameRate(this.callback.getCore(), Path.of(this.url))
            .orElse(30.0);
      } catch (final IOException e) {
        Logger.info("Unable to calculate default frame rate for video! (%s)".formatted(this.url));
        e.printStackTrace();
      }
    }
    this.dims = this.dims == null ? this.callback.getDimensions() : this.dims;
  }

  public Callback getCallback() {
    return this.callback;
  }

  public Dimension getDims() {
    return this.dims;
  }

  public String getUrl() {
    return this.url;
  }

  public String getKey() {
    return this.key;
  }

  public int getRate() {
    return this.rate;
  }

  public static final class VLCMediaFactory extends VideoFactory {

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      return new VLCMediaPlayer(this.getCallback(), this.getDims(),
          this.getUrl(),
          this.getKey(), this.getRate());
    }
  }

  public static final class FFmpegMediaFactory extends VideoFactory {

    private int bufferSize = 10;

    public VideoFactory buffer(final int bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      return new FFmpegMediaPlayer(this.getCallback(), this.getDims(),
          this.bufferSize, this.getUrl(),
          this.getKey(), this.getRate());
    }
  }

  public static final class JCodecMediaFactory extends VideoFactory {

    private int bufferSize = 15;

    public VideoFactory buffer(final int bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      return new JCodecMediaPlayer(this.getCallback(), this.getDims(),
          this.bufferSize, this.getUrl(),
          this.getKey(), this.getRate());
    }
  }


}
