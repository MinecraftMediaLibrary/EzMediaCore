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
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JCodecMediaPlayer extends MediaPlayer {

  private final ArrayBlockingQueue<int[]> frames;
  private final long delay;
  private final int buffer;

  private FrameGrab grabber;
  private boolean paused;
  private long start;
  private volatile CompletableFuture<Void> framePlayer;
  private volatile CompletableFuture<Void> audioPlayer;
  private volatile boolean firstFrame;

  JCodecMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Dimension pixelDimension,
      final int buffer,
      @NotNull final String url,
      @Nullable final String key,
      final int fps) {
    super(callback, pixelDimension, url, key, fps);
    this.buffer = buffer;
    this.frames = new ArrayBlockingQueue<>(buffer * fps);
    this.delay = 1000L / fps;
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
        this.paused = false;
        this.play();
        this.start = System.currentTimeMillis();
      }
      case PAUSE -> {
        this.stopAudio();
        this.paused = true;
        this.start = System.currentTimeMillis();
      }
      case RESUME -> {
        this.initializePlayer(System.currentTimeMillis() - this.start);
        this.paused = false;
        this.play();
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
    final Dimension dimension = this.getDimensions();
    CompletableFuture.runAsync(() -> {
      Picture frame;
      while (!this.paused) {
        try {
          if ((frame = this.grabber.getNativeFrame()) == null) {
            break;
          }
          while (JCodecMediaPlayer.this.frames.remainingCapacity() <= 1) {
            try {
              TimeUnit.MILLISECONDS.sleep(5);
            } catch (final InterruptedException e) {
              e.printStackTrace();
            }
          }
          JCodecMediaPlayer.this.frames.add(VideoFrameUtils.toResizedColorArray(frame, dimension));
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void play() {
    CompletableFuture.runAsync(() -> {
      CompletableFuture.runAsync(this::runPlayer);
      this.delayFrames();
      this.audioPlayer = this.updateAudioPlayer();
      this.framePlayer = this.updateFramePlayer();
    });
  }

  private void delayFrames() {
    final int target = (this.buffer * this.getFrameRate()) >> 1;
    while (true) {
      if (this.frames.size() == target) { // block until frame size met
        break;
      }
    }
  }

  private @NotNull CompletableFuture<Void> updateAudioPlayer() {
    if (this.audioPlayer != null && !this.audioPlayer.isDone()) {
      this.audioPlayer.cancel(true);
    }
    return CompletableFuture.runAsync(() -> {
      while (true) {
        if (this.firstFrame) {
          this.playAudio();
          this.firstFrame = false;
          break;
        }
      }
    }, ExecutorProvider.SHARED_VIDEO_PLAYER);
  }

  private @NotNull CompletableFuture<Void> updateFramePlayer() {
    final Callback callback = this.getCallback();
    if (this.framePlayer != null && !this.framePlayer.isDone()) {
      this.framePlayer.cancel(true);
    }
    return CompletableFuture.runAsync(() -> {
      while (!this.paused) {
        try {
          callback.process(this.frames.take());
          TimeUnit.MILLISECONDS.sleep(JCodecMediaPlayer.this.delay - 6);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, ExecutorProvider.SHARED_VIDEO_PLAYER);
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
