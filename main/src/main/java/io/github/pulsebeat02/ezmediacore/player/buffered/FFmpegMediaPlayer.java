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

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
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
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

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

  private volatile FFmpeg ffmpeg;
  private volatile FFmpegResultFuture future;

  FFmpegMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final BufferConfiguration buffer,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    super(
        callback,
        viewers,
        pixelDimension,
        buffer,
        fps,
        key,
        new FFmpegMediaPlayerInputParser(callback.getCore()));
  }

  @Override
  public void initializePlayer(
      @NotNull final Input mrl,
      @NotNull final DelayConfiguration delay,
      @NotNull final Object @NotNull ... arguments) {
    final MediaRequest request = RequestUtils.requestMediaInformation(mrl);
    this.setDirectVideoMrl(request.getVideoLinks().get(0));
    this.setDirectAudioMrl(request.getAudioLinks().get(0));
    this.constructFFmpegProcess(delay);
    this.addExtraArguments(arguments);
  }

  private void addExtraArguments(@NotNull final Object @NotNull ... arguments) {
    for (int i = 1; i < arguments.length; i++) {
      this.ffmpeg.addArgument(arguments[i].toString());
    }
  }

  private void constructFFmpegProcess(@NotNull final DelayConfiguration delay) {
    final long ms = delay.getDelay() * 1000;
    this.ffmpeg = new FFmpeg(this.getCore().getFFmpegPath().toAbsolutePath());
    this.addInput(ms);
    this.addOutput();
    this.addDimensionArguments();
    this.addMiscArguments();
    this.getCore()
        .getLogger()
        .info(
            Locale.FINISHED_FFMPEG_PROCESS_CREATION.build(
                UnsafeUtils.getFieldExceptionally(this.ffmpeg, "additionalArguments").toString()));
  }

  private void addMiscArguments() {
    this.ffmpeg
        .setLogLevel(LogLevel.FATAL)
        .setProgressListener((line) -> {})
        .setOutputListener(line -> this.getCore().getLogger().ffmpegPlayer(line))
        .addArguments(FFmpegArguments.AUDIO_CODEC, this.getAudioFormat())
        .addArguments(FFmpegArguments.AUDIO_BLOCK_SIZE, String.valueOf(this.getAudioBitrate()))
        .addArguments(FFmpegArguments.AUDIO_BITRATE, String.valueOf(this.getAudioBitrate()))
        .addArguments(FFmpegArguments.AUDIO_CHANNELS, String.valueOf(this.getAudioChannels()));
  }

  private void addDimensionArguments() {
    final Dimension dimension = this.getDimensions();
    this.ffmpeg.addArguments(
        "-vf", "scale=%s:%s".formatted(dimension.getWidth(), dimension.getHeight()));
  }

  private void addOutput() {
    this.ffmpeg.addOutput(
        FrameOutput.withConsumer(this.getFrameConsumer())
            .setFrameRate(this.getFrameConfiguration().getFps())
            .disableStream(StreamType.SUBTITLE)
            .disableStream(StreamType.DATA));
  }

  private void addInput(final long ms) {

    final InputParser parser = this.getInputParser();
    final Pair<Object, String[]> pair = parser.parseInput(this.getDirectVideoMrl());

    final BaseInput<?> input = (BaseInput<?>) pair.getKey();
    input.setPosition(ms);
    input.addArgument("-re");

    this.ffmpeg.addInput(input);
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
    this.stopAudio();
    this.setStart(System.currentTimeMillis());
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.ffmpeg == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.setupPlayer();
  }

  @Override
  public void resume(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.resume(mrl, arguments);
    if (this.ffmpeg == null) {
      this.initializePlayer(mrl, DelayConfiguration.ofDelay(this.getStart()), arguments);
    }
    this.setupPlayer();
  }

  private void setupPlayer() {
    this.stopAudio();
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

  @Override
  public @NotNull Identifier<String> getPlayerType() {
    return MediaPlayer.FFMPEG;
  }

  public static final class Builder extends VideoBuilder {

    private BufferConfiguration bufferSize = BufferConfiguration.BUFFER_15;

    public Builder() {}

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
