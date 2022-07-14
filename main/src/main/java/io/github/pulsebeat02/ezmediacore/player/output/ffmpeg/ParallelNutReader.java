package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.github.kokorin.jaffree.Rational;
import com.github.kokorin.jaffree.ffmpeg.ImageFormat;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.nut.MainHeader;
import com.github.kokorin.jaffree.nut.NutFrame;
import com.github.kokorin.jaffree.nut.NutInputStream;
import com.github.kokorin.jaffree.nut.NutReader;
import com.github.kokorin.jaffree.nut.StreamHeader;
import com.google.common.collect.Lists;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.utility.graphics.VideoFrameUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParallelNutReader {

  private final NativeFrameConsumer consumer;
  private final ImageFormat format;

  ParallelNutReader(final NativeFrameConsumer consumer, final ImageFormat format) {
    this.consumer = consumer;
    this.format = format;
  }

  @Contract("_, _ -> new")
  public static @NotNull ParallelNutReader ofReader(
      @NotNull final NativeFrameConsumer consumer, @NotNull final ImageFormat format) {
    return new ParallelNutReader(consumer, format);
  }

  public void read(final InputStream input) {
    try {
      final NutInputStream stream = new NutInputStream(input);
      final NutReader nutReader = new NutReader(stream);
      final MainHeader mainHeader = nutReader.getMainHeader();
      final StreamHeader[] streamHeaders = nutReader.getStreamHeaders();
      final List<Stream> streams = this.parseTracks(mainHeader, streamHeaders);
      this.readStreams(streams);
      this.readFrames(nutReader, streamHeaders);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
    this.consumer.consume(null);
  }

  private void readStreams(@NotNull final List<Stream> streams) {
    this.consumer.consumeStreams(
        streams.stream().map(s -> (FFmpegBufferedStream) FFmpegMediaStream.ofStream(s)).toList());
  }

  private void readFrames(@NotNull final NutReader nutReader, @NotNull final StreamHeader[] streamHeaders) throws IOException {
    NutFrame nutFrame;
    while ((nutFrame = nutReader.readFrame()) != null) {
      final int trackNo = nutFrame.streamId;
      final FFmpegBufferedFrame frame = this.parseFrame(streamHeaders[trackNo], nutFrame);
      if (frame == null) {
        continue;
      }
      this.consumer.consume(frame);
    }
  }

  private @NotNull List<Stream> parseTracks(
      @NotNull final MainHeader main, @NotNull final StreamHeader[] headers) {
    final List<Stream> result = Lists.newArrayList();
    for (final StreamHeader streamHeader : headers) {
      this.deserializeStream(streamHeader)
          .ifPresent(stream -> this.modifyStream(stream, main, streamHeader));
    }
    return result;
  }

  private void modifyStream(
      @NotNull final Stream stream,
      @NotNull final MainHeader main,
      @NotNull final StreamHeader header) {
    final Rational timebase = main.timeBases[header.timeBaseId];
    stream.setId(header.streamId).setTimebase(timebase.getDenominator() / timebase.getNumerator());
  }

  private @NotNull Optional<Stream> deserializeStream(@NotNull final StreamHeader streamHeader) {
    if (streamHeader.streamType == StreamHeader.Type.VIDEO) {
      return Optional.of(
          this.handleVideoStream(streamHeader.video.width, streamHeader.video.height));
    } else if (streamHeader.streamType == StreamHeader.Type.AUDIO) {
      return Optional.of(
          this.handleAudioStream(streamHeader.audio.sampleRate, streamHeader.audio.channelCount));
    }
    return Optional.empty();
  }

  private @NotNull Stream handleVideoStream(final int width, final int height) {
    return new Stream().setType(Stream.Type.VIDEO).setWidth(width).setHeight(height);
  }

  private @NotNull Stream handleAudioStream(@NotNull final Rational rate, final int channels) {
    return new Stream()
        .setType(Stream.Type.AUDIO)
        .setSampleRate(rate.longValue())
        .setChannels(channels);
  }

  private FFmpegBufferedFrame parseFrame(
      @NotNull final StreamHeader track, @Nullable final NutFrame frame) {

    if (frame == null || frame.data == null || frame.data.length == 0 || frame.eor) {
      return null;
    }

    int[] image = null;
    ByteBufCarrier samples = null;

    if (track.streamType == StreamHeader.Type.VIDEO) {
      final int width = track.video.width;
      final int height = track.video.height;
      image = VideoFrameUtils.getRGBParallel(this.format.toImage(frame.data, width, height));
    } else if (track.streamType == StreamHeader.Type.AUDIO) {
      samples = ByteBufCarrier.ofByteArray(frame.data);
    }

    if (image != null || samples != null) {
      return FFmpegBufferedFrame.ofFrame(track.streamId, frame.pts, image, samples);
    }

    return null;
  }
}
