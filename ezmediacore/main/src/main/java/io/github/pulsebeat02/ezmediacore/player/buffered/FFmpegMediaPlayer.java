/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import com.github.kokorin.jaffree.ffmpeg.ImageFormats;
import com.google.common.collect.Lists;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioOutput;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegArguments;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import io.github.pulsebeat02.ezmediacore.player.input.FFmpegMediaPlayerInputParser;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.output.PlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.StreamOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.*;
import io.github.pulsebeat02.ezmediacore.utility.concurrency.ThrowingRunnable;
import io.github.pulsebeat02.ezmediacore.utility.future.Throwing;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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

  private Process process;

  FFmpegMediaPlayer(
      @NotNull final VideoCallback video,
      @NotNull final AudioSource audio,
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
    this.createProcess(delay, arguments);
    this.execute();
  }

  private void createProcess(
      @NotNull final DelayConfiguration configuration,
      @NotNull final Object @NotNull ... arguments) {

    final Dimension dimension = this.getDimensions();
    final String mrl = this.getInput().getDirectVideoMrl().toString();
    final String delay = String.valueOf(configuration.getDelay() * 1000);

    this.addArgument(this.getCore().getFFmpegPath().toString());
    this.addArgument(FFmpegArguments.HIDE_BANNER);
    this.addArgument(FFmpegArguments.NO_STATS);
    this.addArguments(FFmpegArguments.LOG_LEVEL, "error");
    this.addArguments(FFmpegArguments.NATIVE_FRAME_READ_RATE, mrl);
    this.addArgument(FFmpegArguments.NO_CONSOLE_INPUT);
    this.addArguments(FFmpegArguments.TUNE, "fastdecode");
    this.addArguments(FFmpegArguments.TUNE, "zerolatency");
    this.addArguments(FFmpegArguments.DURATION_START, delay);
    this.addArgument(
        FFmpegArguments.VIDEO_SCALE.formatted(dimension.getWidth(), dimension.getHeight()));
    this.addExtraArguments(arguments);
    this.addArgument(this.getOutput().toString());
  }

  private void addExtraArguments(@NotNull final Object[] arguments) {
    for (int i = 1; i < arguments.length; i++) {
      this.addArgument(arguments[i].toString());
    }
  }

  private void execute() {
    this.startProcess();
    this.handleOutputs();
  }

  private void handleOutputs() {
    final InputStream stream = this.process.getInputStream();
    final FFmpegPlayerOutput raw = this.getRawFFmpegOutput();
    this.setNecessaryFFmpegOutput(stream, raw);
    this.executeNutReader(raw);
  }

  @NotNull
  private FFmpegPlayerOutput getRawFFmpegOutput() {
    final PlayerOutput output = this.getOutput();
    return (FFmpegPlayerOutput) output.getResultingOutput();
  }

  private void setNecessaryFFmpegOutput(
      @NotNull final InputStream stream, @NotNull final FFmpegPlayerOutput raw) {
    final FFmpegOutputConfiguration configuration = raw.getVariedOutput();
    configuration.setOptionalOutput(StreamOutput.ofStream(stream));
  }

  private void executeNutReader(@NotNull final FFmpegPlayerOutput raw) {
    final String internal = raw.getTcp().getResultingOutput().getRaw();
    CompletableFuture.runAsync(this.readNutFormat(internal), ExecutorProvider.FRAME_CONSUMER)
        .handle(Throwing.THROWING_FUTURE);
  }

  private @NotNull Runnable readNutFormat(@NotNull final String internal) {
    return (ThrowingRunnable)
        () -> {
          final InputStream url = new URL(internal).openStream();
          final ParallelNutReader reader =
              ParallelNutReader.ofReader(this.getFrameConsumer(), ImageFormats.BGR24);
          reader.read(url);
        };
  }

  private void startProcess() {
    final ProcessBuilder builder = new ProcessBuilder(this.arguments);
    builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    this.startProcess(builder);
  }

  private void startProcess(@NotNull final ProcessBuilder builder) {
    try {
      this.process = builder.start();
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private void addArgument(@NotNull final String argument) {
    this.arguments.add(argument);
  }

  private void addArguments(@NotNull final String key, @NotNull final String value) {
    this.arguments.add(key);
    this.arguments.add(value);
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull NativeFrameConsumer getFrameConsumer() {
    return new FFmpegFrameConsumer(this);
  }

  @Override
  public void release() {
    super.release();
    if (this.process != null) {
      this.process.destroy();
      this.process.descendants().forEach(ProcessHandle::destroy);
      this.process = null;
    }
  }

  @Override
  public void pause() {
    super.pause();
    if (this.process != null) {
      this.process.destroy();
      this.process.descendants().forEach(ProcessHandle::destroy);
      this.process = null;
    }
    this.setStart(System.currentTimeMillis());
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.process == null) {
      this.initializePlayer(DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.setupPlayer();
  }

  @Override
  public void resume() {
    super.resume();
    if (this.process == null) {
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
    if (this.process != null) {
      this.process.destroy();
      this.process.descendants().forEach(ProcessHandle::destroy);
    }
    this.execute();
  }

  public static final class Builder extends VideoBuilder {

    private BufferConfiguration bufferSize = BufferConfiguration.BUFFER_15;

    public Builder() {}

    @Contract("_ -> this")
    @Override
    public Builder audio(@NotNull final AudioOutput audio) {
      super.audio(audio);
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
    public @NotNull Builder buffer(@NotNull final BufferConfiguration bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      final VideoCallback video = this.getVideo();
      final AudioSource audio = this.getAudio();
      return new FFmpegMediaPlayer(
          video, audio, video.getWatchers(), this.getDims(), this.bufferSize);
    }
  }
}