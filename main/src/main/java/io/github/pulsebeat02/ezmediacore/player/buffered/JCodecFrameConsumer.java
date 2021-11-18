package io.github.pulsebeat02.ezmediacore.player.buffered;

import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
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
import org.jetbrains.annotations.NotNull;

public class JCodecFrameConsumer implements Runnable {

  private final JCodecMediaPlayer player;
  private final FrameGrab grabber;

  JCodecFrameConsumer(@NotNull final JCodecMediaPlayer player, @NotNull final FrameGrab grabber) {
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
      this.player.addFrame(VideoFrameUtils.getRGBParallel(image.get()), this.calculateTimestamp());
      }
    } catch (final IOException | ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private long calculateTimestamp() {
    return Instant.now().toEpochMilli() - this.player.getStart();
  }
}
