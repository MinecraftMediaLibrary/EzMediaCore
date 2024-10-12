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



public final class ParallelNutReader {

  private final NativeFrameConsumer consumer;
  private final ImageFormat format;

  ParallelNutReader(final NativeFrameConsumer consumer, final ImageFormat format) {
    this.consumer = consumer;
    this.format = format;
  }

  @Contract("_, _ -> new")
  public static  ParallelNutReader ofReader(
       final NativeFrameConsumer consumer,  final ImageFormat format) {
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

  private void readStreams( final List<Stream> streams) {
    this.consumer.consumeStreams(
        streams.stream().map(s -> (FFmpegBufferedStream) FFmpegMediaStream.ofStream(s)).toList());
  }

  private void readFrames( final NutReader nutReader,  final StreamHeader[] streamHeaders) throws IOException {
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

  private  List<Stream> parseTracks(
       final MainHeader main,  final StreamHeader[] headers) {
    final List<Stream> result = Lists.newArrayList();
    for (final StreamHeader streamHeader : headers) {
      this.deserializeStream(streamHeader)
          .ifPresent(stream -> this.modifyStream(stream, main, streamHeader));
    }
    return result;
  }

  private void modifyStream(
       final Stream stream,
       final MainHeader main,
       final StreamHeader header) {
    final Rational timebase = main.timeBases[header.timeBaseId];
    stream.setId(header.streamId).setTimebase(timebase.getDenominator() / timebase.getNumerator());
  }

  private  Optional<Stream> deserializeStream( final StreamHeader streamHeader) {
    if (streamHeader.streamType == StreamHeader.Type.VIDEO) {
      return Optional.of(
          this.handleVideoStream(streamHeader.video.width, streamHeader.video.height));
    } else if (streamHeader.streamType == StreamHeader.Type.AUDIO) {
      return Optional.of(
          this.handleAudioStream(streamHeader.audio.sampleRate, streamHeader.audio.channelCount));
    }
    return Optional.empty();
  }

  private  Stream handleVideoStream(final int width, final int height) {
    return new Stream().setType(Stream.Type.VIDEO).setWidth(width).setHeight(height);
  }

  private  Stream handleAudioStream( final Rational rate, final int channels) {
    return new Stream()
        .setType(Stream.Type.AUDIO)
        .setSampleRate(rate.longValue())
        .setChannels(channels);
  }

  private FFmpegBufferedFrame parseFrame(
       final StreamHeader track,  final NutFrame frame) {

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
