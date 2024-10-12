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

import rewrite.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.graphics.VideoFrameUtils;
import io.github.pulsebeat02.ezmediacore.utility.graphics.scalr.AsyncScalr;
import io.github.pulsebeat02.ezmediacore.utility.graphics.scalr.Scalr.Method;
import io.github.pulsebeat02.ezmediacore.utility.graphics.scalr.Scalr.Mode;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;


public class JCodecFrameConsumer implements Runnable {

  private final JCodecMediaPlayer player;
  private final FrameGrab grabber;

  JCodecFrameConsumer( final JCodecMediaPlayer player,  final FrameGrab grabber) {
    this.player = player;
    this.grabber = grabber;
  }

  @Override
  public void run() {
    final Dimension dimensions = this.player.getDimensions();
    try {
      while (!this.player.isExecuting()) {
        // sometimes jcodec returns a null frame...
        final Picture frame = this.grabber.getNativeFrame();
        if (frame == null) {
          break;
        }

        // parallel computation to get bufferedimage
        final Future<BufferedImage> image =
            AsyncScalr.resize(
                VideoFrameUtils.toBufferedImage(frame),
                Method.SPEED,
                Mode.BEST_FIT_BOTH,
                dimensions.getWidth(),
                dimensions.getHeight());

        // add to queue
        this.player.addFrame(
            VideoFrameUtils.getRGBParallel(image.get()), null, this.calculateTimestamp());
      }
    } catch (final IOException | ExecutionException | InterruptedException e) {
      throw new AssertionError(e);
    }
  }

  private long calculateTimestamp() {
    return Instant.now().toEpochMilli() - this.player.getStart();
  }
}
