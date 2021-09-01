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

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FFmpegMediaPlayer extends MediaPlayer {

  private final ArrayBlockingQueue<int[]> frames;
  private final long delay;
  private final int buffer;

  private long start;
  private volatile FFmpeg ffmpeg;
  private volatile FFmpegResultFuture future;
  private volatile CompletableFuture<Void> framePlayer;
  private volatile CompletableFuture<Void> audioPlayer;
  private volatile boolean firstFrame;

  FFmpegMediaPlayer(
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
    this.firstFrame = false;
    this.initializePlayer(0L);
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    this.firstFrame = false;
    switch (controls) {
      case START, RESUME -> this.play(controls);
      case PAUSE -> this.pause();
      case RELEASE -> this.release();
      default -> throw new IllegalArgumentException("Player state is invalid!");
    }
  }

  @Override
  public void initializePlayer(final long seconds) {
    final Dimension dimension = this.getDimensions();
    final String url = this.getUrl();
    final Path path = Path.of(url);
    final long ms = seconds * 1000;
    this.ffmpeg =
        new FFmpeg(this.getCore().getFFmpegPath())
            .addInput(
                Files.exists(path)
                    ? UrlInput.fromPath(path).setPosition(ms)
                    : UrlInput.fromUrl(url).setPosition(ms))
            .addOutput(
                FrameOutput.withConsumer(this.getFrameConsumer())
                    .setFrameRate(this.getFrameRate())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA)
                    .setFrameRate(this.getFrameRate()))
            .addArguments("-vf",
                "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()));
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull FrameConsumer getFrameConsumer() {
    return new FrameConsumer() {
      @Override
      public void consumeStreams(final List<Stream> streams) {
      }

      @Override
      public void consume(final Frame frame) {

        if (frame == null) {
          return;
        }

        final BufferedImage image = frame.getImage();
        if (image == null) {
          return;
        }

        if (!FFmpegMediaPlayer.this.firstFrame) {
          FFmpegMediaPlayer.this.firstFrame = true;
        }

        while (FFmpegMediaPlayer.this.frames.remainingCapacity() <= 1) {
          try {
            TimeUnit.MILLISECONDS.sleep(5);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }

        FFmpegMediaPlayer.this.frames.add(VideoFrameUtils.getRGBParallel(image));
      }
    };
  }

  private void release() {
    if (this.ffmpeg != null) {
      this.future.graceStop();
      this.future = null;
    }
  }

  private void pause() {
    if (this.ffmpeg != null) {
      this.future.graceStop();
    }
    this.stopAudio();
    this.start = System.currentTimeMillis();
  }

  private void play(@NotNull final PlayerControls controls) {
    this.setupPlayer(controls);
    CompletableFuture.runAsync(() -> {
      this.future = this.updateFFmpegPlayer();
      this.delayFrames();
      this.audioPlayer = this.updateAudioPlayer();
      this.framePlayer = this.updateVideoPlayer();
    });
  }

  private void setupPlayer(@NotNull final PlayerControls controls) {
    if (controls == PlayerControls.START) {
      this.stopAudio();
      if (this.ffmpeg == null) {
        this.initializePlayer(0L);
      }
      this.start = 0L;
    } else if (controls == PlayerControls.RESUME) {
      this.initializePlayer(System.currentTimeMillis() - this.start);
    }
  }

  private FFmpegResultFuture updateFFmpegPlayer() {
    if (this.future != null && !this.future.isDone()) {
      this.future.stop(true);
    }
    return this.ffmpeg.executeAsync(ExecutorProvider.SHARED_VIDEO_PLAYER);
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

  private @NotNull CompletableFuture<Void> updateVideoPlayer() {
    final Callback callback = this.getCallback();
    if (this.framePlayer != null && !this.framePlayer.isDone()) {
      this.framePlayer.cancel(true);
    }
    return CompletableFuture.runAsync(() -> {
      while (!this.future.isDone()) {
        try {
          callback.process(this.frames.take());
          TimeUnit.MILLISECONDS.sleep(this.delay - 7);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, ExecutorProvider.SHARED_VIDEO_PLAYER);
  }


}
