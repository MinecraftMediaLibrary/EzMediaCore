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

  protected static VLCMediaPlayer VLC_PLAYER_INSTANCE;

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
      @NotNull final FrameConfiguration fps) {
    super(callback, viewers, pixelDimension, key, fps);
    this.adapter = this.getAdapter();
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
  public void setPlayerState(@NotNull final PlayerControls controls, @NotNull final Object... arguments) {
    super.setPlayerState(controls);
    switch (controls) {
      case START -> {
        this.setMrlConfiguration(ArgumentUtils.checkPlayerArguments(arguments));
        if (this.player == null) {
          this.initializePlayer(0L, this.getProperArguments(arguments));
        }
        this.player.media().play(this.getMrlConfiguration().getMrl());
        this.playAudio();
      }
      case PAUSE -> {
        this.stopAudio();
        this.player.controls().stop();
      }
      case RESUME -> {
        if (this.player == null) {
          this.initializePlayer(0L);
          this.player.media().play(this.getMrlConfiguration().getMrl());
        } else {
          this.player.controls().play();
        }
        this.playAudio();
      }
      case RELEASE -> this.releaseAll();
      default -> throw new IllegalArgumentException("Player state is invalid!");
    }
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
    this.player = this.getEmbeddedMediaPlayer(Arrays.stream(arguments).collect(Collectors.toList()));
    this.player.controls().setTime(ms);
  }

  @Override
  public long getElapsedMilliseconds() {
    return this.player.status().time();
  }

  private @NotNull EmbeddedMediaPlayer getEmbeddedMediaPlayer(@NotNull final Collection<Object> arguments) {
    if (this.player == null || this.factory == null
        || this.logger == null) { // just in case something is null
      this.releaseAll();
      this.factory = new MediaPlayerFactory(this.constructArguments(arguments));
      final NativeLog logger = this.factory.application().newLog();
      if (logger == null) { // ignore this warning as its intellij being dumb with native bindings
        Logger.info("VLC Native Logger not available on this platform!");
      } else {
        logger.setLevel(LogLevel.DEBUG);
        logger.addLogListener(
            (level, module, file, line, name, header, id, message) ->
                Logger.directPrintVLC(
                    "[%-20s] (%-20s) %7s: %s\n".formatted(module, name, level, message)));
      }
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
    return args;
  }

  private void releaseAll() {
    if (this.player != null) {
      this.player.controls().stop();
      this.player.release();
      this.player = null;
    }
    if (this.factory != null) {
      this.factory.release();
      this.player = null;
    }
    if (this.logger != null) {
      this.logger.release();
      this.logger = null;
    }
    this.surface = null;
  }

  @Contract(" -> new")
  private @NotNull CallbackVideoSurface getSurface() {
    if (this.surface == null) {
      this.surface = new CallbackVideoSurface(this.getBufferCallback(), this.videoCallback, false,
          this.adapter);
    }
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
    this.videoCallback = new MinecraftVideoRenderCallback(this, pixels);
    if (this.player == null) {
      this.initializePlayer(0L);
    }
    this.player.videoSurface().set(this.getSurface());
  }

  @Override
  public void setCustomAudioAdapter(@NotNull final Consumer<byte[]> audio, final int blockSize,
      @NotNull final String format, final int rate, final int channels) {
    this.audioCallback = new MinecraftAudioCallback(audio, blockSize);
    this.player.audio().callback(format, rate, channels, this.audioCallback);
  }

  private void modifyPlayerAttributes() {
    this.videoCallback = new MinecraftVideoRenderCallback(this);
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

  private static class MinecraftVideoRenderCallback extends RenderCallbackAdapter {

    private final Consumer<int[]> callback;

    public MinecraftVideoRenderCallback(@NotNull final VLCMediaPlayer player) {
      super(new int[player.getDimensions().getWidth() * player.getDimensions().getHeight()]);
      this.callback = player.getCallback()::process;
    }

    public MinecraftVideoRenderCallback(@NotNull final VLCMediaPlayer player,
        @NotNull final Consumer<int[]> consumer) {
      super(new int[player.getDimensions().getWidth() * player.getDimensions().getHeight()]);
      this.callback = consumer;
    }

    @Override
    protected void onDisplay(
        final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, final int[] buffer) {
      this.callback.accept(buffer);
    }
  }

  private static class MinecraftAudioCallback extends AudioCallbackAdapter {

    private final Consumer<byte[]> callback;
    private final int blockSize;

    public MinecraftAudioCallback(@NotNull final Consumer<byte[]> consumer, final int blockSize) {
      this.callback = consumer;
      this.blockSize = blockSize;
    }

    @Override
    public void play(final uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer,
        final Pointer samples,
        final int sampleCount, final long pts) {
      this.callback.accept(samples.getByteArray(0, sampleCount * this.blockSize));
    }
  }

  public static final class Builder extends VideoBuilder {

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

    /**
     * The reason why we have to make the VLC player instance mutable is
     * due to hard references. By default, JNA requires all native resources
     * to have a strong reference, so they won't be garbage collected. That way,
     * the JVM won't crash when native resources try to interact with a JVM
     * object that doesn't exist. Therefore, as hideous as it sounds, we must
     * use a mutable static object to maintain such a hard reference. I hope
     * to find a better solution to solve this in the future.
     *
     * @return the video player
     */
    @Contract(" -> new")
    @Override
    public @NotNull MediaPlayer build() {
      final Callback callback = this.getCallback();
      final Viewers viewers = callback.getWatchers();
      final Dimension dims = this.getDims();
      final SoundKey key = this.getKey();
      final FrameConfiguration rate = this.getRate();
      if (VLC_PLAYER_INSTANCE == null) {
        VLC_PLAYER_INSTANCE = new VLCMediaPlayer(callback, viewers, dims,
          key, rate);
      } else {
        VLC_PLAYER_INSTANCE.setCallback(callback);
        VLC_PLAYER_INSTANCE.setViewers(viewers);
        VLC_PLAYER_INSTANCE.setDimensions(dims);
        VLC_PLAYER_INSTANCE.setSoundKey(key);
        VLC_PLAYER_INSTANCE.setFrameConfiguration(rate);
      }
      return VLC_PLAYER_INSTANCE;
    }
  }
}
