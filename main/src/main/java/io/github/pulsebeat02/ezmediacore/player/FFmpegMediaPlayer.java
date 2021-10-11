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
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.throwable.InvalidStreamHeaderException;
import io.github.pulsebeat02.ezmediacore.utility.ArgumentUtils;
import io.github.pulsebeat02.ezmediacore.utility.Pair;
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

/*

Algorithm for frame consuming into a player. We can retrieve the frames,
and put them into a ArrayBlockingQueue. We store the ArrayBlockingQueue
with both int[] as the frame data, and also a double for the timestamp.
Next, we keep track of an internal time.

Timestamp for each frame is calculated with the following equation.
Suppose the function T(f) takes in a frame called f and returns the
timestamp of that specific frame.

T(f) = f.pts * f.timebase

We can calculate this asynchronously when we fetch the frame as it
can be slow doing the decimal division. We put these into our data
structure to store the next frames.

For our display thread, we can continuously fetch from our frame
data structure. We have to account for some cases which may lead
to the downfall of the program.

What if the data structure is empty?
In this rare case where FFmpeg isn't fast enough to deliver frames
compared to display frames, we are lucky our data structure is
blocking, so we just block the thread until we are able to get the
next frame.

What if we are behind in frames?
This is a bit harder, as it requires likely a faster algorithm. One
way to do this is by looping over the next frames while keeping track
of the current time (Instant.now()) and trying to display the frame
after the frame that matched up our time. Obviously, this may be a
bit slow, and that overtime for the loop also takes some time to
process too, so we must account for that as well.
 */
public final class FFmpegMediaPlayer extends MediaPlayer implements BufferedPlayer {

  private ArrayBlockingQueue<Pair<int[], Long>> frames;
  private BufferConfiguration buffer;
  private long delay;
  private long start;
  private long startEpoch;
  private volatile FFmpeg ffmpeg;
  private volatile FFmpegResultFuture future;
  private volatile CompletableFuture<Void> framePlayer;
  private volatile CompletableFuture<Void> audioPlayer;
  private volatile boolean firstFrame;

  FFmpegMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @Nullable final SoundKey key,
      @NotNull final FrameConfiguration fps) {
    super(callback, viewers, pixelDimension, key, fps);
    this.buffer = buffer;
    this.modifyPlayerAttributes();
  }

  @Override
  public @NotNull BufferConfiguration getBufferConfiguration() {
    return this.buffer;
  }

  @Override
  public void setBufferConfiguration(@NotNull final BufferConfiguration configuration) {
    this.buffer = configuration;
    this.modifyPlayerAttributes();
  }

  private void modifyPlayerAttributes() {
    final int fps = this.getFrameConfiguration().getFps();
    this.frames = new ArrayBlockingQueue<>(this.buffer.getBuffer() * fps);
    this.delay = 1000L / fps;
    this.firstFrame = false;
  }

  @Override
  public void setDimensions(@NotNull final Dimension dimensions) {
    super.setDimensions(dimensions);
    this.modifyPlayerAttributes();
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls,
      @NotNull final Object... arguments) {
    super.setPlayerState(controls);
    CompletableFuture.runAsync(() -> {
      this.firstFrame = false;
      switch (controls) {
        case START, RESUME -> this.play(controls, arguments);
        case PAUSE -> this.pause();
        case RELEASE -> this.release();
        default -> throw new IllegalArgumentException("Player state is invalid!");
      }
    });
  }

  @Override
  public void initializePlayer(final long seconds, @NotNull final Object... arguments) {
    this.setDirectVideoMrl(ArgumentUtils.retrieveDirectVideo(arguments));
    this.setDirectAudioMrl(ArgumentUtils.retrieveDirectAudio(arguments));
    final Dimension dimension = this.getDimensions();
    final String url = this.getDirectVideoMrl().getMrl();
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
                    .setFrameRate(this.getFrameConfiguration().getFps())
                    .disableStream(StreamType.AUDIO)
                    .disableStream(StreamType.SUBTITLE)
                    .disableStream(StreamType.DATA)
                    .setFrameRate(this.getFrameConfiguration().getFps()))
            .addArguments("-vf",
                "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()))
            .setLogLevel(LogLevel.FATAL)
            .setProgressListener((line) -> {
            })
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
        final int max = streams.stream().mapToInt(Stream::getId).max().orElseThrow(
            InvalidStreamHeaderException::new);

        // create our lookup table
        this.streams = new Stream[max];

        // loop through elements, set id with proper stream
        for (final Stream stream : streams) {
          this.streams[stream.getId()] = stream;
        }
      }

      @Override
      public void consume(@Nullable final Frame frame) {

        // sometimes ffmpeg returns a null frame...
        if (frame == null) {
          return;
        }

        // sometimes it is an audio frame (or non-video frame in general). We don't want audio frames
        final BufferedImage image = frame.getImage();
        if (image == null) {
          return;
        }

        // audio frame
        if (!FFmpegMediaPlayer.this.firstFrame) {
          FFmpegMediaPlayer.this.firstFrame = true;
          FFmpegMediaPlayer.this.startEpoch = Instant.now().toEpochMilli();
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
            Pair.ofPair(
                VideoFrameUtils.getRGBParallel(image),
                (long)((frame.getPts() * (1.0F / this.streams[frame.getStreamId()].getTimebase())) * 1000)
            ));
      }
    };
  }

  private void release() {
    if (this.ffmpeg != null && this.future != null) {
      this.future.graceStop();
      this.future = null;
    }
  }

  private void pause() {
    if (this.future != null) {
      this.future.graceStop();
    }
    this.stopAudio();
    this.start = System.currentTimeMillis();
  }

  private void play(@NotNull final PlayerControls controls, @NotNull final Object[] arguments) {
    this.setupPlayer(controls, arguments);
    this.future = this.updateFFmpegPlayer();
    this.delayFrames();
    this.audioPlayer = this.updateAudioPlayer();
    this.framePlayer = this.updateVideoPlayer();
  }

  private void setupPlayer(@NotNull final PlayerControls controls,
      @NotNull final Object[] arguments) {
    if (controls == PlayerControls.START) {
      this.stopAudio();
      if (this.ffmpeg == null) {
        this.initializePlayer(0L, arguments);
      }
      this.start = 0L;
    } else if (controls == PlayerControls.RESUME) {
      this.initializePlayer(System.currentTimeMillis() - this.start, arguments);
    }
  }

  private FFmpegResultFuture updateFFmpegPlayer() {
    if (this.future != null && !this.future.isDone()) {
      this.future.stop(true);
    }
    return this.ffmpeg.executeAsync(ExecutorProvider.ENCODER_HANDLER);
  }

  private void delayFrames() {
    final int target = (this.buffer.getBuffer() * this.getFrameConfiguration().getFps()) >> 1;
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
    }, ExecutorProvider.AUDIO_HANDLER);
  }

  private @NotNull CompletableFuture<Void> updateVideoPlayer() {
    final Callback callback = this.getCallback();
    if (this.framePlayer != null && !this.framePlayer.isDone()) {
      this.framePlayer.cancel(true);
    }
    return CompletableFuture.runAsync(() -> {
      while (!this.future.isDone()) {
        try {

          // process current frame
          final Pair<int[], Long> frame = this.frames.take();
          callback.process(frame.getKey());

          // wait delay
          TimeUnit.MILLISECONDS.sleep(this.delay - 10);

          // skip frames if necessary
          // For example, if the passed time is 40 ms, and the time of the
          // current frame is 30 ms, we have to skip. If it is equal, we are
          // bound to get behind, so skip
          final long passed = Instant.now().toEpochMilli() - this.startEpoch;
          Pair<int[], Long> skip = this.frames.peek();
          while (skip.getValue() <= passed) {
            skip = this.frames.poll();
          }

        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, ExecutorProvider.FRAME_HANDLER);
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

    Builder() {
    }

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
      return new FFmpegMediaPlayer(callback, callback.getWatchers(), this.getDims(),
          this.bufferSize,
          this.getKey(), this.getRate());
    }
  }

}
