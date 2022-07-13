package io.github.pulsebeat02.ezmediacore.player.output.ffmpeg;

import com.github.kokorin.jaffree.Rational;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.nut.*;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class NutFormatReader extends NutFrameReader {

  private final FrameConsumer consumer;
  private final ImageFormat format;

  public NutFormatReader(final FrameConsumer consumer, final ImageFormat format) {
    super(consumer, format);
    this.consumer = consumer;
    this.format = format;
  }

  @Override
  public void read(final InputStream input) throws IOException {
    final NutInputStream stream = new NutInputStream(input);
    final NutReader nutReader = new NutReader(stream);
    final MainHeader mainHeader = nutReader.getMainHeader();
    final StreamHeader[] streamHeaders = nutReader.getStreamHeaders();
    final List<Stream> streams = this.parseTracks(mainHeader, streamHeaders);
    this.consumer.consumeStreams(streams);
    NutFrame nutFrame;
    while ((nutFrame = nutReader.readFrame()) != null) {
      final int trackNo = nutFrame.streamId;
      final Frame frame = this.parseFrame(streamHeaders[trackNo], nutFrame);
      if (frame == null) {
        continue;
      }
      this.consumer.consume(frame);
    }
    this.consumer.consume(null);
  }

  private List<Stream> parseTracks(final MainHeader main, final StreamHeader[] headers) {

    final List<Stream> result = Lists.newArrayList();



    for (final StreamHeader streamHeader : headers) {
      final Stream stream = this.deserializeStream(streamHeader);
      if (stream != null) {
        final Rational timebase = main.timeBases[streamHeader.timeBaseId];
        stream
            .setId(streamHeader.streamId)
            .setTimebase(timebase.getDenominator() / timebase.getNumerator());
        result.add(stream);
      }
    }

    return result;
  }

  @Nullable
  private Stream deserializeStream(@NotNull final StreamHeader streamHeader) {
    if (streamHeader.streamType == StreamHeader.Type.VIDEO) {
      return this.handleVideoStream(streamHeader.video.width, streamHeader.video.height);
    } else if (streamHeader.streamType == StreamHeader.Type.AUDIO) {
      return this.handleAudioStream(streamHeader.audio.sampleRate, streamHeader.audio.channelCount);
    }
    return null;
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

  private Frame parseFrame(final StreamHeader track, final NutFrame frame) {
    if (frame == null || frame.data == null || frame.data.length == 0 || frame.eor) {
      return null;
    }

    BufferedImage image = null;
    int[] samples = null;

    if (track.streamType == StreamHeader.Type.VIDEO) {
      final int width = track.video.width;
      final int height = track.video.height;
      if (frame.data.length == width * height * this.format.getBytesPerPixel()) {
        image = this.format.toImage(frame.data, width, height);
      }
    } else if (track.streamType == StreamHeader.Type.AUDIO) {
      final ByteBuffer data = ByteBuffer.wrap(frame.data);
      final IntBuffer intData = data.asIntBuffer();
      samples = new int[intData.limit()];
      intData.get(samples);
    }

    if (image != null || samples != null) {
      return new Frame(track.streamId, frame.pts, image, samples);
    }

    return null;
  }
}
