package io.github.pulsebeat02.ezmediacore.player;

import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidStreamHeaderException;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FFmpegFrameConsumer implements FrameConsumer {

  private final FFmpegMediaPlayer player;
  private float[] calculations;

  FFmpegFrameConsumer(@NotNull final FFmpegMediaPlayer player) {
    this.player = player;
  }

  @Override
  public void consumeStreams(final List<Stream> streams) {
    // if stream ids are not properly ordered, sometimes we need to rearrange them
    final int max =
        streams.stream()
            .mapToInt(Stream::getId)
            .max()
            .orElseThrow(InvalidStreamHeaderException::new);

    // create our lookup table
    this.calculations = new float[max + 1];

    // loop through elements, set id with proper stream timebase
    for (final Stream stream : streams) {
      this.calculations[stream.getId()] = (1.0F / stream.getTimebase()) * 1000;
    }

    // set start time
    this.player.setStart(Instant.now().toEpochMilli());

    // play audio
    this.player.playAudio();
  }

  @Override
  public void consume(final Frame frame) {
    // sometimes ffmpeg returns a null frame...
    if (frame == null) {
      return;
    }

    // sometimes it is an audio frame (or non-video frame in general). We don't want audio
    // frames
    final BufferedImage image = frame.getImage();
    if (image == null) {
      return;
    }

    // add to queue
    this.player.addFrame(VideoFrameUtils.getRGBParallel(image), this.calculateTimeStamp(frame));
  }

  private long calculateTimeStamp(@NotNull final Frame frame) {
    return (long) (frame.getPts() * this.calculations[frame.getStreamId()]);
  }
}
