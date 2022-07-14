package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegBufferedFrame;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegBufferedStream;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.NativeFrameConsumer;
import io.github.pulsebeat02.ezmediacore.throwable.IllegalStreamHeaderException;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FFmpegFrameConsumer implements NativeFrameConsumer {

  private final FFmpegMediaPlayer player;
  private float[] calculations;

  FFmpegFrameConsumer(@NotNull final FFmpegMediaPlayer player) {
    this.player = player;
  }

  @Override
  public void consumeStreams(final List<FFmpegBufferedStream> streams) {
    // if stream ids are not properly ordered, sometimes we need to rearrange them
    final int max =
        streams.stream()
            .mapToInt(FFmpegBufferedStream::getId)
            .max()
            .orElseThrow(IllegalStreamHeaderException::new);

    // create our lookup table
    this.calculations = new float[max + 1];

    // loop through elements, set id with proper stream timebase
    for (final FFmpegBufferedStream stream : streams) {
      this.calculations[stream.getId()] = (1.0F / stream.getTimebase()) * 1000;
    }

    // set start time
    this.player.setStart(Instant.now().toEpochMilli());
  }

  @Override
  public void consume(final FFmpegBufferedFrame frame) {

    // sometimes ffmpeg returns a null frame...
    if (frame == null) {
      return;
    }

    final byte[] audio = frame.getSamples().getByteArray();
    final int[] image = frame.getImage();

    // add to queue
    this.player.addFrame(image, audio, this.calculateTimeStamp(frame));
  }

  private long calculateTimeStamp(@NotNull final FFmpegBufferedFrame frame) {
    return (long) (frame.getPts() * this.calculations[frame.getStreamId()]);
  }
}
