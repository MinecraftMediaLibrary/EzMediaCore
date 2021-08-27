/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.player;

import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JCodecMediaPlayer extends MediaPlayer {

  private FrameGrab grabber;

  private boolean paused;
  private long start;

  JCodecMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Dimension pixelDimension,
      final int buffer,
      @NotNull final String url,
      @Nullable final String key,
      final int fps) {
    super(callback, pixelDimension, url, key, fps);
    this.initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    switch (controls) {
      case START -> {
        if (this.grabber == null) {
          this.initializePlayer(0L);
        }
        CompletableFuture.runAsync(this::runPlayer);
        this.start = System.currentTimeMillis();
      }
      case PAUSE -> {
        this.stopAudio();
        this.paused = true;
        this.start = System.currentTimeMillis();
      }
      case RESUME -> {
        this.paused = false;
        this.initializePlayer(System.currentTimeMillis() - this.start);
        CompletableFuture.runAsync(this::runPlayer);
      }
      case RELEASE -> {
        this.paused = false;
        if (this.grabber != null) {
          this.grabber = null;
        }
      }
      default -> throw new IllegalArgumentException("Player state is invalid!");
    }
  }

  private void runPlayer() {

    this.playAudio();

    final Dimension dimension = this.getDimensions();
    final int width = dimension.getWidth();
    final int height = dimension.getHeight();

    this.playAudio();

    CompletableFuture.runAsync(() -> {
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
    });

  }

  @Override
  public void initializePlayer(final long ms) {
    final Dimension dimension = this.getDimensions();
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
