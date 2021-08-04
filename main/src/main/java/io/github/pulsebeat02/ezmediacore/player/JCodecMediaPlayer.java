package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.FrameCallback;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.NotNull;

public class JCodecMediaPlayer extends MediaPlayer {

  private FrameGrab grabber;

  private boolean paused;
  private long start;

  JCodecMediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final String url,
      final int frameRate) {
    super(core, callback, url, frameRate);
    this.initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START:
        if (this.grabber == null) {
          this.initializePlayer(0L);
        }
        CompletableFuture.runAsync(this::runPlayer);
        this.start = System.currentTimeMillis();
        break;
      case PAUSE:
        this.stopAudio();
        this.paused = true;
        this.start = System.currentTimeMillis();
        break;
      case RESUME:
        this.paused = false;
        this.initializePlayer(System.currentTimeMillis() - this.start);
        CompletableFuture.runAsync(this::runPlayer);
        break;
      case RELEASE:
        this.paused = false;
        this.grabber = null;
        break;
    }
  }

  private void runPlayer() {

    this.playAudio();

    final ImmutableDimension dimension = this.getDimensions();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();

    this.playAudio();

    Picture picture;
    while (!this.paused) {
      try {
        if ((picture = this.grabber.getNativeFrame()) == null) {
          break;
        }
        this.getCallback()
            .process(
                VideoFrameUtils.toBufferedImage(picture)
                    .getRGB(0, 0, width, height, null, 0, width));
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void initializePlayer(final long ms) {
    final ImmutableDimension dimension = this.getDimensions();
    this.start = ms;
    try {
      this.grabber = FrameGrab.createFrameGrab(NIOUtils.readableFileChannel(this.getUrl()));
      this.grabber.seekToSecondPrecise(ms / 1000.0F);
      this.grabber.getMediaInfo().setDim(new Size(dimension.getWidth(), dimension.getHeight()));
    } catch (final IOException | JCodecException e) {
      e.printStackTrace();
    }
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }
}
