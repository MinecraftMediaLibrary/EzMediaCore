package io.github.pulsebeat02.epicmedialib.player;

import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.callback.FrameCallback;
import io.github.pulsebeat02.epicmedialib.utility.ImmutableDimension;
import io.github.pulsebeat02.epicmedialib.utility.VideoFrameUtils;
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

  public JCodecMediaPlayer(
      @NotNull final MediaLibraryCore core,
      @NotNull final FrameCallback callback,
      @NotNull final ImmutableDimension dimensions,
      @NotNull final String url,
      final int frameRate) {
    super(core, callback, dimensions, url, frameRate);
    initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START:
        if (this.grabber == null) {
          initializePlayer(0L);
        }
        CompletableFuture.runAsync(this::runPlayer);
        break;
      case PAUSE:
        stopAudio();
        this.paused = true;
        break;
      case RESUME:
        this.paused = false;
        final long current = System.currentTimeMillis();
        initializePlayer(current - this.start);
        this.start = current;
        CompletableFuture.runAsync(this::runPlayer);
        break;
      case RELEASE:
        this.paused = false;
        this.grabber = null;
        break;
    }
  }

  private void runPlayer() {

    playAudio();

    final ImmutableDimension dimension = getDimensions();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();

    playAudio();

    Picture picture;
    while (!this.paused) {
      try {
        if ((picture = this.grabber.getNativeFrame()) == null) {
          break;
        }
        getCallback()
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
    final ImmutableDimension dimension = getDimensions();
    this.start = ms;
    try {
      this.grabber = FrameGrab.createFrameGrab(NIOUtils.readableFileChannel(getUrl()));
      this.grabber.seekToSecondPrecise(ms / 1000.0F);
      this.grabber.getMediaInfo().setDim(new Size(dimension.getWidth(), dimension.getHeight()));
    } catch (final IOException | JCodecException e) {
      e.printStackTrace();
    }
  }
}
