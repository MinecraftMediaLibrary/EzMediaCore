package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.time.Instant;
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
    try {
      while (!this.player.isExecuting()) {
        // sometimes jcodec returns a null frame...
        final Picture frame = this.grabber.getNativeFrame();
        if (frame == null) {
          break;
        }

        // add to queue
        this.player.addFrame(
            VideoFrameUtils.toResizedColorArray(frame, this.player.getDimensions()),
            Instant.now().toEpochMilli() - this.player.getStart());
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
