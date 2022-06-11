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
package io.github.pulsebeat02.ezmediacore.player.external;

import static com.google.common.base.Preconditions.checkArgument;

import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.CoreLogger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.DelayConfiguration;
import io.github.pulsebeat02.ezmediacore.callback.Identifier;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.player.FrameConfiguration;
import io.github.pulsebeat02.ezmediacore.player.MediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.SoundKey;
import io.github.pulsebeat02.ezmediacore.player.VideoBuilder;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.player.input.InputParser;
import io.github.pulsebeat02.ezmediacore.player.input.VLCMediaPlayerInputParser;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import io.github.pulsebeat02.ezmediacore.utility.tuple.Pair;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public final class VLCMediaPlayer extends MediaPlayer implements ConsumablePlayer {

  private final VideoSurfaceAdapter adapter;
  private BufferFormatCallback bufferFormatCallback;
  private MinecraftVideoRenderCallback videoCallback;
  private MinecraftAudioCallback audioCallback;
  private MediaPlayerFactory factory;
  private EmbeddedMediaPlayer player;
  private NativeLog logger;

  VLCMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final FrameConfiguration fps,
      @Nullable final SoundKey key) {
    super(callback, viewers, pixelDimension, fps, key, new VLCMediaPlayerInputParser(callback.getCore()));
    this.adapter = this.getAdapter();
    this.videoCallback = new MinecraftVideoRenderCallback(this.getCallback()::process);
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
    this.setDirectVideoMrl(RequestUtils.getVideoURLs(mrl).get(0));
    this.setDirectAudioMrl(RequestUtils.getAudioURLs(mrl).get(0));
    if (this.player == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
    }
    this.playAudio();
    this.playMedia(true);
  }

  @Override
  public void pause() {
    super.pause();
    this.stopAudio();
    this.player.controls().stop();
  }

  @Override
  public void resume(@NotNull final Input mrl, @NotNull final Object... arguments) {
    super.resume(mrl, arguments);
    if (this.player == null) {
      this.initializePlayer(mrl, DelayConfiguration.DELAY_0_MS, arguments);
      this.playAudio();
      this.playMedia(true);
    } else {
      this.playAudio();
      this.playMedia(false);
    }
  }

  private void playMedia(final boolean newMedia) {
    if (newMedia) {
      final InputParser parser = this.getInputParser();
      final Pair<Object, String[]> pair = parser.parseInput(this.getDirectVideoMrl());
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

  @Override
  public void initializePlayer(
      @NotNull final Input mrl,
      @NotNull final DelayConfiguration delay,
      @NotNull final Object... arguments) {
    this.player = this.getEmbeddedMediaPlayer(
        Arrays.stream(arguments).collect(Collectors.toList()));
    this.player.controls().setTime(delay.getDelay());
    this.player.videoSurface().set(this.getSurface());
  }

  @Override
  public long getElapsedMilliseconds() {
    return this.player.status().time();
  }

  @Override
  public @NotNull Identifier<String> getPlayerType() {
    return MediaPlayer.VLC;
  }

  @Override
  public boolean isBuffered() {
    return false;
  }

  private @NotNull EmbeddedMediaPlayer getEmbeddedMediaPlayer(
      @NotNull final Collection<Object> arguments) {
    if (this.player == null || this.factory == null || this.logger == null) { // just in case something is null;
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
    final List<String> args = new ArrayList<>();

    final int rate = this.getFrameConfiguration().getFps();
    if (rate > 0) {
      args.add("sout=\"#transcode{fps=%d}\"".formatted(rate));
    }

    if (this.audioCallback == null) {
      args.add("--no-audio");
    }

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
    return new CustomBufferFormatCallback(dimension);
  }

  @Override
  public void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels) {
    this.checkIfReleased();
    this.videoCallback = new MinecraftVideoRenderCallback(pixels);
    this.player.videoSurface().set(this.getSurface());
  }

  @Override
  public void setCustomAudioAdapter(@NotNull final Consumer<byte[]> audio, @NotNull final String format, final int blockSize,
       final int rate, final int channels) {
    this.checkIfReleased();
    this.audioCallback = new MinecraftAudioCallback(audio, blockSize);
    this.player.audio().callback(format, rate, channels, this.audioCallback);
  }

  private void checkIfReleased() {
    checkArgument(this.player != null, "Cannot modify player after being released!");
  }

  private void modifyPlayerAttributes() {
    this.setCustomVideoAdapter(this.getCallback()::process);
  }

  @Override
  public void setCallback(@NotNull final Callback callback) {
    super.setCallback(callback);
    this.modifyPlayerAttributes();
  }

  private record CustomBufferFormatCallback(
      Dimension dimension) implements
      BufferFormatCallback {

    private CustomBufferFormatCallback(@NotNull final Dimension dimension) {
      this.dimension = dimension;
    }

    @Override
    public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
      return new RV32BufferFormat(this.dimension.getWidth(), this.dimension.getHeight());
    }

    @Override
    public void allocatedBuffers(final ByteBuffer[] buffers) {
    }
  }

  public static final class Builder extends VideoBuilder {

    public Builder() {
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

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      final Callback callback = this.getCallback();
      return new VLCMediaPlayer(callback, callback.getWatchers(), this.getDims(), this.getRate(), this.getKey());
    }
  }

  private class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    MinecraftVideoRenderCallback(@NotNull final Consumer<int[]> consumer) {
      this(consumer, VLCMediaPlayer.super.getDimensions().getWidth(),
          VLCMediaPlayer.super.getDimensions().getHeight());
    }

    MinecraftVideoRenderCallback(@NotNull final Consumer<int[]> consumer, final int width, final int height) {
      super(new int[height * width]);
      this.callback = consumer;
    }

    @Override
    protected void onDisplay(
        final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, final int[] buffer) {
      this.callback.accept(buffer);
    }

    public @NotNull Consumer<int[]> getCallback() {
      return this.callback;
    }
  }

  private class MinecraftAudioCallback extends AudioCallbackAdapter {

    private final Consumer<byte[]> callback;
    private final Viewers viewers;
    private final int blockSize;

    MinecraftAudioCallback(@NotNull final Consumer<byte[]> consumer, final int blockSize) {
      this.callback = consumer;
      this.viewers = VLCMediaPlayer.super.getWatchers();
      this.blockSize = blockSize;
    }

    @Override
    public void play(
        @NotNull final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer,
        @NotNull final Pointer samples,
        final int sampleCount, final long pts) {
      this.callback.accept(samples.getByteArray(0, sampleCount * this.blockSize));
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
}
