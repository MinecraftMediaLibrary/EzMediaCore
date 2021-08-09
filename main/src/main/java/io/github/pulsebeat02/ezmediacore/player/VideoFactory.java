package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.VideoPlayerOption;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class VideoFactory {

  private FrameCallback callback;
  private Dimension dims;
  private VideoPlayerOption option = VideoPlayerOption.NOT_SPECIFIED;
  private String url;
  private String key;
  private int rate = 25;

  public static VideoFactory builder() {
    return new VideoFactory();
  }

  public VideoFactory callback(@NotNull final FrameCallback callback) {
    this.callback = callback;
    return this;
  }

  public VideoFactory url(@NotNull final String url) {
    this.url = url;
    return this;
  }

  public VideoFactory mode(@NotNull final VideoPlayerOption option) {
    this.option = option;
    return this;
  }

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

  public MediaPlayer build() {
    Objects.requireNonNull(this.callback);
    final MediaLibraryCore core = this.callback.getCore();
    final Dimension dims = this.dims == null ? this.callback.getDimensions() : this.dims;
    return switch (this.option) {
      case NOT_SPECIFIED -> switch (core.getDiagnostics().getSystem().getOSType()) {
        case MAC, WINDOWS -> new VLCMediaPlayer(this.callback, dims, this.url, this.key, this.rate);
        case UNIX -> new FFmpegMediaPlayer(this.callback, dims, this.url, this.key, this.rate);
      };
      case VLC -> new VLCMediaPlayer(this.callback, dims, this.url, this.key, this.rate);
      case FFMPEG -> new FFmpegMediaPlayer(this.callback, dims, this.url, this.key, this.rate);
      case JCODEC -> new JCodecMediaPlayer(this.callback, dims, this.url, this.key, this.rate);
    };
  }
}
