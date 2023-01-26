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
package io.github.pulsebeat02.ezmediacore.player.external;

import com.google.common.collect.Lists;
import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.CoreLogger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.VideoCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioCallback;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.player.input.VLCMediaPlayerInputParser;
import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCMediaFrame;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.base.callback.AudioCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.LinuxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.OsxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public final class VLCMediaPlayer extends MediaPlayer {

  private final VideoSurfaceAdapter adapter;
  private BufferFormatCallback bufferFormatCallback;
  private final MinecraftVideoRenderCallback videoCallback;
  private MediaPlayerFactory factory;
  private EmbeddedMediaPlayer player;
  private NativeLog logger;

  VLCMediaPlayer(
      @NotNull final VideoCallback video,
      @NotNull final AudioCallback audio,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension) {
    super(video, audio, viewers, pixelDimension,
        new VLCMediaPlayerInputParser(video.getCore()));
    this.adapter = this.getAdapter();
    this.videoCallback = new MinecraftVideoRenderCallback(this, this.getVideoCallback()::process);
  }

  private VideoSurfaceAdapter getAdapter() {
    return switch (this.getCore().getDiagnostics().getSystem().getOSType()) {
      case MAC -> new OsxVideoSurfaceAdapter();
      case UNIX -> new LinuxVideoSurfaceAdapter();
      case WINDOWS -> new WindowsVideoSurfaceAdapter();
    };
  }

  @Override
  public void start(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.start(mrl, arguments);
    if (this.player == null) {
      this.initializePlayer(DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.playMedia(true);
  }

  @Override
  public void pause() {
    super.pause();
    this.player.controls().stop();
  }

  @Override
  public void resume() {
    super.resume();
    if (this.player == null) {
      this.initializePlayer(DelayConfiguration.DELAY_0_MS);
      this.playMedia(true);
    } else {
      this.playMedia(false);
    }
  }

  private void playMedia(final boolean newMedia) {
    if (newMedia) {
      final InputParser parser = this.getInputParser();
      final Pair<Object, String[]> pair = parser.parseInput(this.getInput().getDirectVideoMrl());
      this.player.media().play((String) pair.getKey(), pair.getValue());
    } else {
      this.player.controls().play();
    }
  }

  @Override
  public void release() {
    super.release();
    if (this.player != null) {
      this.player.controls().stop();
      this.logger.release();
      this.player.release();
      this.factory.release();
    }
  }

  public void initializePlayer(
      @NotNull final DelayConfiguration delay,
      @NotNull final Object... arguments) {
    this.player = this.getEmbeddedMediaPlayer(
            Stream.of(arguments).collect(Collectors.toList()));
    this.player.controls().setTime(delay.getDelay());
    this.player.videoSurface().set(this.getSurface());
    this.player.audio().callback("wav", 160000, 2, this.createAudioCallback());
  }

  private @NotNull MinecraftAudioCallback createAudioCallback() {
    final Consumer<byte[]> audio = this.getAudioCallback()::process;
    return new MinecraftAudioCallback(this, audio, 3840);
  }

  private @NotNull EmbeddedMediaPlayer getEmbeddedMediaPlayer(
      @NotNull final Collection<Object> arguments) {
    if (this.player == null || this.factory == null
        || this.logger == null) { // just in case something is null;
      this.initializeFactory(arguments);
      this.initializeLogger();
      return this.factory.mediaPlayers().newEmbeddedMediaPlayer();
    }
    return this.player;
  }

  private void initializeFactory(@NotNull final Collection<Object> arguments) {
    this.factory = new MediaPlayerFactory(this.constructArguments(arguments));
  }

  private void initializeLogger() {

    this.logger = this.factory.application().newLog();
    this.logger.setLevel(LogLevel.DEBUG);

    final MediaLibraryCore core = this.getCore();
    final CoreLogger logger = core.getLogger();
    final String format = "[%-20s] (%-20s) %7s: %s%s";
    this.logger.addLogListener(
        (level, module, file, line, name, header, id, message) ->
            logger.vlc(format.formatted(module, name, level, message, System.lineSeparator())));
  }

  private @NotNull List<String> constructArguments(@NotNull final Collection<Object> arguments) {
    final String output = this.getOutput().toString();
    final List<String> args = Lists.newArrayList(output);
    args.addAll(arguments.stream().map(Object::toString).collect(Collectors.toList()));
    return args;
  }

  @Contract(" -> new")
  private @NotNull CallbackVideoSurface getSurface() {
    return new CallbackVideoSurface(this.getBufferCallback(),
        this.videoCallback, false,
        this.adapter);
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull BufferFormatCallback getBufferCallback() {
    if (this.bufferFormatCallback == null) {
      this.bufferFormatCallback = this.createBufferedCallback();
    }
    return this.bufferFormatCallback;
  }

  private @NotNull BufferFormatCallback createBufferedCallback() {
    final Dimension dimension = VLCMediaPlayer.this.getDimensions();
    return new MinecraftVideoCallback(dimension);
  }

  private record MinecraftVideoCallback(
      Dimension dimension) implements
      BufferFormatCallback {

    private MinecraftVideoCallback(@NotNull final Dimension dimension) {
      this.dimension = dimension;
    }

    @Override
    public @NotNull BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
      return new RV32BufferFormat(this.dimension.getWidth(), this.dimension.getHeight());
    }

    @Override
    public void allocatedBuffers(final ByteBuffer[] buffers) {
    }
  }

  private class MinecraftAudioCallback extends AudioCallbackAdapter {

    private final ConsumableOutput output;
    private final Consumer<byte[]> callback;
    private final Viewers viewers;
    private final int blockSize;

    MinecraftAudioCallback(
        @NotNull final VLCMediaPlayer player,
        @NotNull final Consumer<byte[]> consumer,
        final int blockSize) {
      this.output = (ConsumableOutput) player.getOutput().getResultingOutput();
      this.callback = consumer;
      this.viewers = player.getWatchers();
      this.blockSize = blockSize;
    }

    @Override
    public void play(
        @NotNull final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer,
        @NotNull final Pointer samples,
        final int sampleCount, final long pts) {
      final byte[] arr = samples.getByteArray(0, sampleCount * this.blockSize);
      this.callback.accept(arr);
      this.output.consume(VLCMediaFrame.ofFrame(null, arr));
    }

    public @NotNull Consumer<byte[]> getCallback() {
      return this.callback;
    }

    public @NotNull Viewers getViewers() {
      return this.viewers;
    }

    public int getBlockSize() {
      return this.blockSize;
    }
  }

  private class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final ConsumableOutput output;
    private final Consumer<int[]> callback;

    MinecraftVideoRenderCallback(@NotNull final VLCMediaPlayer player, @NotNull final Consumer<int[]> consumer) {
      this(player, consumer, VLCMediaPlayer.super.getDimensions().getWidth(),
          VLCMediaPlayer.super.getDimensions().getHeight());
    }

    MinecraftVideoRenderCallback(@NotNull final VLCMediaPlayer player, @NotNull final Consumer<int[]> consumer, final int width,
        final int height) {
      super(new int[height * width]);
      this.output = (ConsumableOutput) player.getOutput().getResultingOutput();
      this.callback = consumer;
    }

    @Override
    protected void onDisplay(
        final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, final int[] buffer) {
      this.callback.accept(buffer);
      this.output.consume(VLCMediaFrame.ofFrame(buffer, null));
    }

    public @NotNull Consumer<int[]> getCallback() {
      return this.callback;
    }
  }

  public static final class Builder extends VideoBuilder {

    public Builder() {
    }

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

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      final VideoCallback video = this.getVideo();
      final AudioCallback audio = this.getAudio();
      return new VLCMediaPlayer(video, audio, video.getWatchers(), this.getDims());
    }
  }
}
