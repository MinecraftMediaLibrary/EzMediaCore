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

import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.player.input.JCodecMediaPlayerInputParser;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Size;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This video player sucks as it ignores all output configurations, which means that there is not
 * even audio at all...
 */
public final class JCodecMediaPlayer extends BufferedMediaPlayer {

  private FrameGrab grabber;

  JCodecMediaPlayer(
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
        new JCodecMediaPlayerInputParser(video.getCore()));
  }

  @Override
  public void release() {
    super.release();
    this.forceStop();
    if (this.grabber != null) {
      this.grabber = null;
    }
  }

  @Override
  public void resume() {
    super.resume();
    this.initializePlayer(DelayConfiguration.ofDelay(this.getStart()));
    this.play();
  }

  @Override
  public void pause() {
    super.pause();
    this.forceStop();
    this.setStart(System.currentTimeMillis());
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.grabber == null) {
      this.initializePlayer(DelayConfiguration.DELAY_0_MS);
    }
    this.play();
    this.setStart(System.currentTimeMillis());
  }

  private void runPlayer() {
    CompletableFuture.runAsync(
        new JCodecFrameConsumer(this, this.grabber), ExecutorProvider.ENCODER_HANDLER);
  }

  private void play() {
    this.runPlayer();
    this.bufferFrames();
    this.startDisplayRunnable();
    this.startWatchdogRunnable();
  }

  public void initializePlayer(@NotNull final DelayConfiguration configuration) {
    final Dimension dimension = this.getDimensions();
    try {
      this.grabber = this.getGrabber();
      this.grabber.seekToSecondPrecise(configuration.getDelay() / 1000.0F);
      this.grabber.getMediaInfo().setDim(new Size(dimension.getWidth(), dimension.getHeight()));
      this.getCore().getLogger().info(Locale.FINISHED_JCODEC_FRAME_GRABBER.build());
    } catch (final IOException | JCodecException e) {
      throw new AssertionError(e);
    }
  }

  private @NotNull FrameGrab getGrabber() throws IOException, JCodecException {
    return FrameGrab.createFrameGrab(this.parseInput());
  }

  private @NotNull FileChannelWrapper parseInput() throws FileNotFoundException {
    final InputParser parser = this.getInputParser();
    final Pair<Object, String[]> pair = parser.parseInput(this.getInput().getDirectVideoMrl());
    return NIOUtils.readableChannel(Path.of((String) pair.getKey()).toFile());
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
    public @NotNull Builder buffer(@NotNull final BufferConfiguration bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      final VideoCallback video = this.getVideo();
      final AudioCallback audio = this.getAudio();
      return new JCodecMediaPlayer(
          video, audio, video.getWatchers(), this.getDims(), this.bufferSize);
    }
  }
}
