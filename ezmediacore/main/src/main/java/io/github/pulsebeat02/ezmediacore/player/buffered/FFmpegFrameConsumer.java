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
package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegBufferedFrame;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegBufferedStream;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.NativeFrameConsumer;
import io.github.pulsebeat02.ezmediacore.throwable.IllegalStreamHeaderException;
import java.time.Instant;
import java.util.List;


public final class FFmpegFrameConsumer implements NativeFrameConsumer {

  private final FFmpegMediaPlayer player;
  private float[] calculations;

  FFmpegFrameConsumer( final FFmpegMediaPlayer player) {
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

  private long calculateTimeStamp( final FFmpegBufferedFrame frame) {
    return (long) (frame.getPts() * this.calculations[frame.getStreamId()]);
  }
}
