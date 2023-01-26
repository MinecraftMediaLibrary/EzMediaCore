package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.github.kokorin.jaffree.ffmpeg.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FFmpegMediaStream implements FFmpegBufferedStream {

  private final int id;
  private final long timebase;

  FFmpegMediaStream(final int id, final long timebase) {
    this.id = id;
    this.timebase = timebase;
  }

  @Contract("_ -> new")
  public static @NotNull FFmpegMediaStream ofStream(@NotNull final Stream stream) {
    return new FFmpegMediaStream(stream.getId(), stream.getTimebase());
  }

  @Contract("_, _ -> new")
  public static @NotNull FFmpegMediaStream ofStream(final int id, final long timebase) {
    return new FFmpegMediaStream(id, timebase);
  }


  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public long getTimebase() {
    return this.timebase;
  }
}
