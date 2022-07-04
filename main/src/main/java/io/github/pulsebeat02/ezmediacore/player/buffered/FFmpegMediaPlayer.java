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
package io.github.pulsebeat02.ezmediacore.player.buffered;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.BaseInput;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.google.common.collect.Lists;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import io.github.pulsebeat02.ezmediacore.player.input.FFmpegMediaPlayerInputParser;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
public final class FFmpegMediaPlayer extends BufferedMediaPlayer {

  private final List<String> arguments;

  private volatile FFmpeg ffmpeg;
  private volatile FFmpegResultFuture future;

  FFmpegMediaPlayer(
      @NotNull final VideoCallback video,
      @NotNull final AudioCallback audio,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer) {
    super(
        video,
        audio,
        viewers,
        pixelDimension,
        buffer,
        new FFmpegMediaPlayerInputParser(video.getCore()));
    this.arguments = Lists.newArrayList();
  }

  public void initializePlayer(
      @NotNull final DelayConfiguration delay, @NotNull final Object @NotNull ... arguments) {
    this.addExtraArguments(arguments);
  }

  private void addExtraArguments(@NotNull final Object @NotNull ... arguments) {
    for (int i = 1; i < arguments.length; i++) {
      this.ffmpeg.addArgument(arguments[i].toString());
    }
  }

  private void createProcess(@NotNull final DelayConfiguration configuration) {

    final Dimension dimension = this.getDimensions();
    final String mrl = this.getInput().getDirectVideoMrl().toString();
    final String delay = String.valueOf(configuration.getDelay() * 1000);

    this.addArgument(this.getCore().getFFmpegPath().toString());
    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArgument(FFmpegArguments.NATIVE_FRAME_READ_RATE);
    this.addArguments(FFmpegArguments.INPUT, mrl);
    this.addArgument(FFmpegArguments.NO_CONSOLE_INPUT);
    this.addArguments(FFmpegArguments.TUNE, "fastdecode");
    this.addArguments(FFmpegArguments.TUNE, "zerolatency");
    this.addArguments(FFmpegArguments.DURATION_START, delay);
    this.addArgument(FFmpegArguments.VIDEO_SCALE.formatted(dimension.getWidth(), dimension.getHeight()));
    this.addArgument(this.getOutput().toString());

    // handle output...
  }

  private void addArgument(@NotNull final String argument) {
    this.arguments.add(argument);
  }

  private void addArguments(@NotNull final String key, @NotNull final String value) {
    this.arguments.add(key);
    this.arguments.add(value);
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull FrameConsumer getFrameConsumer() {
    return new FFmpegFrameConsumer(this);
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
    this.setStart(System.currentTimeMillis());
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.ffmpeg == null) {
      this.initializePlayer(DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.setupPlayer();
  }

  @Override
  public void resume() {
    super.resume();
    if (this.ffmpeg == null) {
      this.initializePlayer(DelayConfiguration.ofDelay(this.getStart()));
    }
    this.setupPlayer();
  }

  private void setupPlayer() {
    this.updateFFmpegPlayer();
    this.bufferFrames();
    this.startDisplayRunnable();
    this.startWatchdogRunnable();
  }

  private void updateFFmpegPlayer() {
    if (this.future != null) {
      this.cancelFuture(this.future.toCompletableFuture());
    }
    this.future = this.ffmpeg.executeAsync(ExecutorProvider.ENCODER_HANDLER);

    final CompletableFuture<FFmpegResult> completableFuture = this.future.toCompletableFuture();
    completableFuture.handle(Throwing.THROWING_FUTURE);
  }

  public static final class Builder extends VideoBuilder {

    private BufferConfiguration bufferSize = BufferConfiguration.BUFFER_15;

    public Builder() {}

    @Contract("_ -> this")
    @Override
    public Builder audio(@NotNull final AudioCallback callback) {
      super.audio(callback);
      return this;
    }

    @Contract("_ -> this")
    @Override
    public Builder video(@NotNull final VideoCallback callback) {
      super.video(callback);
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
      final VideoCallback video = this.getVideo();
      final AudioCallback audio = this.getAudio();
      return new FFmpegMediaPlayer(
          video, audio, video.getWatchers(), this.getDims(), this.bufferSize);
    }
  }
}
