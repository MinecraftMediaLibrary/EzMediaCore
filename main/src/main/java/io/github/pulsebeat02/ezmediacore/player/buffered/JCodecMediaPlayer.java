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

import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
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
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
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
import org.jetbrains.annotations.Nullable;

public final class JCodecMediaPlayer extends BufferedMediaPlayer {

  private FrameGrab grabber;

  JCodecMediaPlayer(
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
        new JCodecMediaPlayerInputParser(callback.getCore()));
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
  public void resume(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.resume(mrl, arguments);
    this.initializePlayer(mrl, DelayConfiguration.ofDelay(this.getStart()), arguments);
    this.play();
  }

  @Override
  public void pause() {
    super.pause();
    this.stopAudio();
    this.forceStop();
    this.setStart(System.currentTimeMillis());
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    this.setDirectVideoMrl(RequestUtils.getVideoURLs(mrl).get(0));
    this.setDirectAudioMrl(RequestUtils.getAudioURLs(mrl).get(0));
    if (this.grabber == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
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

  @Override
  public void initializePlayer(
      @NotNull final Input mrl,
      @NotNull final DelayConfiguration configuration,
      @NotNull final Object... arguments) {
    final Dimension dimension = this.getDimensions();
    try {
      this.grabber = this.getGrabber();
      this.grabber.seekToSecondPrecise(configuration.getDelay() / 1000.0F);
      this.grabber.getMediaInfo().setDim(new Size(dimension.getWidth(), dimension.getHeight()));
      this.getCore().getLogger().info(Locale.FINISHED_JCODEC_FRAME_GRABBER);
    } catch (final IOException | JCodecException e) {
      throw new AssertionError(e);
    }
  }

  private @NotNull FrameGrab getGrabber() throws IOException, JCodecException {
    return FrameGrab.createFrameGrab(this.parseInput());
  }

  private @NotNull FileChannelWrapper parseInput() throws FileNotFoundException {
    final InputParser parser = this.getInputParser();
    final Pair<Object, String[]> pair = parser.parseInput(this.getDirectVideoMrl());
    return NIOUtils.readableChannel(Path.of((String) pair.getKey()).toFile());
  }

  @Override
  public @NotNull Identifier<String> getPlayerType() {
    return MediaPlayer.JCODEC;
  }

  @Deprecated
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
    public @NotNull Builder buffer(@NotNull final BufferConfiguration bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      super.init();
      final Callback callback = this.getCallback();
      return new JCodecMediaPlayer(
          callback,
          callback.getWatchers(),
          this.getDims(),
          this.bufferSize,
          this.getRate(),
          this.getKey());
    }
  }
}
