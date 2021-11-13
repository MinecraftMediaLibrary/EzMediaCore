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

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.Stream;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidStreamHeaderException;
import io.github.pulsebeat02.ezmediacore.utility.Pair;
import io.github.pulsebeat02.ezmediacore.utility.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.VideoFrameUtils;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FFmpegMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private final ArrayBlockingQueue<Pair<int[], Long>> frames;
  private final BufferConfiguration buffer;
  private long start;

  private volatile FFmpeg ffmpeg;
  private volatile FFmpegResultFuture future;
  private volatile CompletableFuture<Void> framePlayer;

  FFmpegMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    super(callback, viewers, pixelDimension, fps, key);
    this.buffer = buffer;
    this.frames =
        new ArrayBlockingQueue<>(this.buffer.getBuffer() * this.getFrameConfiguration().getFps());
  }

  @Override
  public @NotNull BufferConfiguration getBufferConfiguration() {
    return this.buffer;
  }

  @Override
  public void initializePlayer(
      @NotNull final MrlConfiguration mrl,
      @NotNull final DelayConfiguration delay,
      @NotNull final Object... arguments) {
    this.setDirectVideoMrl(RequestUtils.getVideoURLs(mrl).get(0));
    this.setDirectAudioMrl(RequestUtils.getAudioURLs(mrl).get(0));
    final Dimension dimension = this.getDimensions();
    final String url = this.getDirectVideoMrl().getMrl();
    final Path path = Path.of(url);
    final long ms = delay.getDelay() * 1000;
    this.ffmpeg =
        new FFmpeg(this.getCore().getFFmpegPath().toAbsolutePath())
            .addInput(
                (Files.exists(path)
                        ? UrlInput.fromPath(path).setPosition(ms)
                        : UrlInput.fromUrl(url).setPosition(ms))
                    .addArgument("-re"))
            .addOutput(
                FrameOutput.withConsumer(this.getFrameConsumer())
                    .setFrameRate(this.getFrameConfiguration().getFps())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA))
            .addArguments(
                "-vf", "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()))
            .setLogLevel(LogLevel.FATAL)
            .setProgressListener((line) -> {})
            .setOutputListener(Logger::directPrintFFmpegPlayer);
    for (int i = 1; i < arguments.length; i++) {
      this.ffmpeg.addArgument(arguments[i].toString());
    }
  }

  @Override
  public long getElapsedMilliseconds() {
    return System.currentTimeMillis() - this.start;
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull FrameConsumer getFrameConsumer() {
    return new FrameConsumer() {

      private Stream[] streams;

      @Override
      public void consumeStreams(@NotNull final List<Stream> streams) {

        // if stream ids are not properly ordered, sometimes we need to rearrange them
        final int max =
            streams.stream()
                .mapToInt(Stream::getId)
                .max()
                .orElseThrow(InvalidStreamHeaderException::new);

        // create our lookup table
        this.streams = new Stream[max + 1];

        // loop through elements, set id with proper stream
        for (final Stream stream : streams) {
          this.streams[stream.getId()] = stream;
        }

        // set start time
        FFmpegMediaPlayer.this.start = Instant.now().toEpochMilli();

        // play audio
        FFmpegMediaPlayer.this.playAudio();
      }

      @Override
      public void consume(@Nullable final Frame frame) {

        // sometimes ffmpeg returns a null frame...
        if (frame == null) {
          return;
        }

        // sometimes it is an audio frame (or non-video frame in general). We don't want audio
        // frames
        final BufferedImage image = frame.getImage();
        if (image == null) {
          return;
        }

        // hack to wait for the player for frames.
        while (FFmpegMediaPlayer.this.frames.remainingCapacity() <= 1) {
          try {
            TimeUnit.MILLISECONDS.sleep(5);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }

        // add to queue
        FFmpegMediaPlayer.this.frames.add(
            Pair.ofPair(VideoFrameUtils.getRGBParallel(image), this.calculateTimeStamp(frame)));
      }

      private long calculateTimeStamp(@NotNull final Frame frame) {
        return (long)
            ((frame.getPts() * (1.0F / this.streams[frame.getStreamId()].getTimebase())) * 1000);
      }
    };
  }

  @Override
  public void release() {
    super.release();
    if (this.ffmpeg != null && this.future != null) {
      this.future.graceStop();
      this.future = null;
    }
  }

  @Override
  public void pause() {
    super.pause();
    if (this.future != null) {
      this.future.graceStop();
    }
    this.stopAudio();
    this.start = System.currentTimeMillis();
  }

  @Override
  public void start(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.ffmpeg == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.setupPlayer();
  }

  @Override
  public void resume(@NotNull final MrlConfiguration mrl, @NotNull final Object... arguments) {
    super.resume(mrl, arguments);
    if (this.ffmpeg == null) {
      this.initializePlayer(
          mrl, DelayConfiguration.ofDelay(System.currentTimeMillis() - this.start), arguments);
    }
    this.setupPlayer();
  }

  private void setupPlayer() {
    this.stopAudio();
    this.future = this.updateFFmpegPlayer();
    this.delayFrames();
    this.framePlayer = this.updateVideoPlayer();
  }

  private void delayFrames() {
    final int target = (this.buffer.getBuffer() * this.getFrameConfiguration().getFps()) >> 1;
    while (this.frames.size() >= target) {}
  }

  private @NotNull FFmpegResultFuture updateFFmpegPlayer() {
    this.checkCancellableFuture(this.future.toCompletableFuture());
    return this.ffmpeg.executeAsync(ExecutorProvider.ENCODER_HANDLER);
  }

  private @NotNull CompletableFuture<Void> updateVideoPlayer() {
    this.checkCancellableFuture(this.framePlayer);
    return CompletableFuture.allOf(
        CompletableFuture.runAsync(this.getDisplayRunnable(), ExecutorProvider.FRAME_HANDLER),
        CompletableFuture.runAsync(this.getSkipRunnable(), ExecutorProvider.FRAME_HANDLER));
  }

  private <T> void checkCancellableFuture(@Nullable final CompletableFuture<T> future) {
    if (future != null && !future.isDone()) {
      future.cancel(true);
    }
  }

  @Contract(pure = true)
  private @NotNull Runnable getDisplayRunnable() {
    return () -> {
      final Callback callback = this.getCallback();
      while (!this.future.isDone()) {
        try {
          // process current frame
          callback.process(this.frames.take().getKey());
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Contract(pure = true)
  private @NotNull Runnable getSkipRunnable() {
    return () -> {
      while (!this.future.isDone()) {
        // skip frames if necessary
        // For example, if the passed time is 40 ms, and the time of the
        // current frame is 30 ms, we have to skip. If it is equal, we are
        // bound to get behind, so skip
        try {
          final long passed = Instant.now().toEpochMilli() - this.start;
          Pair<int[], Long> skip = this.frames.take();
          while (skip.getValue() <= passed) {
            for (int i = 0; i <= 5; i++) {
              skip = this.frames.take();
            }
          }
        } catch (final InterruptedException ignored) {
        }
      }
    };
  }

  @Override
  public @NotNull PlayerType getPlayerType() {
    return PlayerType.FFMPEG;
  }

  @Override
  public boolean isBuffered() {
    return true;
  }

  public static final class Builder extends VideoBuilder {

    private BufferConfiguration bufferSize = BufferConfiguration.BUFFER_15;

    Builder() {}

    @Contract("_ -> this")
    @Override
    public Builder callback(@NotNull final Callback callback) {
      super.callback(callback);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder frameRate(@NotNull final FrameConfiguration rate) {
      super.frameRate(rate);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder dims(@NotNull final Dimension dims) {
      super.dims(dims);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder soundKey(@NotNull final SoundKey key) {
      super.soundKey(key);
      return this;
    }

    @Contract("_ -> this")
    public @NotNull VideoBuilder buffer(@NotNull final BufferConfiguration bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      final Callback callback = this.getCallback();
      return new FFmpegMediaPlayer(
          callback,
          callback.getWatchers(),
          this.getDims(),
          this.bufferSize,
          this.getRate(),
          this.getKey());
    }
  }
}
