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

import com.google.common.base.Preconditions;
import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.callback.Callback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.ArgumentUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
  private CallbackVideoSurface surface;
  private MediaPlayerFactory factory;
  private EmbeddedMediaPlayer player;
  private NativeLog logger;

  VLCMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @Nullable final SoundKey key,
      @NotNull final FrameConfiguration fps,
      @Nullable final Object... arguments) {
    super(callback, viewers, pixelDimension, key, fps);
    this.adapter = this.getAdapter();
    this.initializePlayer(0L, arguments == null ? new String[]{} : arguments);
    this.modifyPlayerAttributes();
  }

  private VideoSurfaceAdapter getAdapter() {
    return switch (this.getCore().getDiagnostics().getSystem().getOSType()) {
      case MAC -> new OsxVideoSurfaceAdapter();
      case UNIX -> new LinuxVideoSurfaceAdapter();
      case WINDOWS -> new WindowsVideoSurfaceAdapter();
    };
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls,
      @NotNull final Object... arguments) {
    super.setPlayerState(controls);
    CompletableFuture.runAsync(() -> {
      switch (controls) {
        case START -> {
          this.setDirectVideoMrl(ArgumentUtils.retrieveDirectVideo(arguments));
          this.setDirectAudioMrl(ArgumentUtils.retrieveDirectAudio(arguments));
          if (this.player == null) {
            this.initializePlayer(0L, this.getProperArguments(arguments));
          }
          this.playAudio();
          this.player.media().play(this.getDirectVideoMrl().getMrl());
        }
        case PAUSE -> {
          this.stopAudio();
          this.player.controls().stop();
        }
        case RESUME -> {
          if (this.player == null) {
            this.initializePlayer(0L);
            this.playAudio();
            this.player.media().play(this.getDirectVideoMrl().getMrl());
          } else {
            this.playAudio();
            this.player.controls().play();
          }
        }
        case RELEASE -> this.releaseAll();
        default -> throw new IllegalArgumentException("Player state is invalid!");
      }
    });
  }

  private Object @NotNull [] getProperArguments(final Object @NotNull [] arguments) {
    final Object[] args = new Object[arguments.length - 1];
    if (args.length - 1 >= 0) {
      System.arraycopy(arguments, 1, args, 0, args.length - 1);
    }
    return args;
  }

  @Override
  public void initializePlayer(final long ms, @NotNull final Object... arguments) {
    this.player = this.getEmbeddedMediaPlayer(
        Arrays.stream(arguments).collect(Collectors.toList()));
    this.player.controls().setTime(ms);
  }

  @Override
  public long getElapsedMilliseconds() {
    return this.player.status().time();
  }

  private @NotNull EmbeddedMediaPlayer getEmbeddedMediaPlayer(
      @NotNull final Collection<Object> arguments) {
    if (this.player == null || this.factory == null || this.logger == null) { // just in case something is null;
      this.factory = new MediaPlayerFactory(this.constructArguments(arguments));
      this.logger = this.factory.application().newLog();
      this.logger.setLevel(LogLevel.DEBUG);
      this.logger.addLogListener(
          (level, module, file, line, name, header, id, message) ->
              Logger.directPrintVLC(
                  "[%-20s] (%-20s) %7s: %s\n".formatted(module, name, level, message)));
      return this.factory.mediaPlayers().newEmbeddedMediaPlayer();
    }
    return this.player;
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
    args.add("--verbose=0");
    args.add("--file-logging");
    args.add("--logfile=%s".formatted(Logger.getVlcLoggerPath()));
    return args;
  }

  private void releaseAll() {
    if (this.player != null) {
      this.player.controls().stop();
      this.logger.release();
      this.player.release();
      this.factory.release();
    }
  }

  @Contract(" -> new")
  private @NotNull CallbackVideoSurface getSurface() {
    this.surface = new CallbackVideoSurface(this.getBufferCallback(), this.videoCallback, false,
        this.adapter);
    return this.surface;
  }

  @Contract(value = " -> new", pure = true)
  private @NotNull BufferFormatCallback getBufferCallback() {
    if (this.bufferFormatCallback == null) {
      final Dimension dimension = VLCMediaPlayer.this.getDimensions();
      this.bufferFormatCallback = new BufferFormatCallback() {
        @Override
        public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
          return new RV32BufferFormat(dimension.getWidth(), dimension.getHeight());
        }

        @Override
        public void allocatedBuffers(final ByteBuffer[] buffers) {
        }
      };
    }
    return this.bufferFormatCallback;
  }

  @Override
  public void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels) {
    this.checkIfReleased();
    this.videoCallback = new MinecraftVideoRenderCallback(pixels);
    this.player.videoSurface().set(this.getSurface());
  }

  @Override
  public void setCustomAudioAdapter(@NotNull final Consumer<byte[]> audio, final int blockSize,
      @NotNull final String format, final int rate, final int channels) {
    this.checkIfReleased();
    this.audioCallback = new MinecraftAudioCallback(audio, blockSize);
    this.player.audio().callback(format, rate, channels, this.audioCallback);
  }

  private void checkIfReleased() {
    Preconditions.checkArgument(this.player != null, "Cannot modify player after being released!");
  }

  private void modifyPlayerAttributes() {
    this.setCustomVideoAdapter(this.getCallback()::process);
  }

  @Override
  public void setCallback(@NotNull final Callback callback) {
    super.setCallback(callback);
    this.modifyPlayerAttributes();
  }

  @Override
  public void setDimensions(@NotNull final Dimension dimensions) {
    super.setDimensions(dimensions);
    this.modifyPlayerAttributes();
  }

  public static final class Builder extends VideoBuilder {

    private Object[] arguments = {};

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
    public Builder args(@NotNull final Object... arguments) {
      this.arguments = arguments;
      return this;
    }

    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      final Callback callback = this.getCallback();
      return new VLCMediaPlayer(callback, callback.getWatchers(), this.getDims(), this.getKey(),
          this.getRate(),
          this.arguments);
    }
  }

  private class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    public MinecraftVideoRenderCallback() {
      this(VLCMediaPlayer.super.getCallback()::process);
    }

    public MinecraftVideoRenderCallback(@NotNull final Consumer<int[]> consumer) {
      super(new int[VLCMediaPlayer.super.getDimensions().getWidth()
          * VLCMediaPlayer.super.getDimensions().getHeight()]);
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

    public MinecraftAudioCallback(@NotNull final Consumer<byte[]> consumer, final int blockSize) {
      this.callback = consumer;
      this.viewers = VLCMediaPlayer.super.getWatchers();
      this.blockSize = blockSize;
    }

    @Override
    public void play(final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer,
        final Pointer samples,
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
