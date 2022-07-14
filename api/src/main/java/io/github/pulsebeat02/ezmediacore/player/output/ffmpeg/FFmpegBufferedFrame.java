package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FFmpegBufferedFrame {

  private final int streamId;
  private final long pts;
  private final int[] image;
  private final BufferCarrier samples;

  FFmpegBufferedFrame(final int streamId, final long pts, final int @NotNull [] image, @NotNull final BufferCarrier samples) {
    this.streamId = streamId;
    this.pts = pts;
    this.image = image;
    this.samples = samples;
  }

  @Contract("_, _, _, _ -> new")
  public static @NotNull FFmpegBufferedFrame ofFrame(final int streamId, final long pts, final int @NotNull [] image, @NotNull final BufferCarrier samples) {
    return new FFmpegBufferedFrame(streamId, pts, image, samples);
  }

  public int getStreamId() {
    return this.streamId;
  }

  public long getPts() {
    return this.pts;
  }

  public int @NotNull [] getImage() {
    return this.image;
  }

  public @NotNull BufferCarrier getSamples() {
    return this.samples;
  }
}
