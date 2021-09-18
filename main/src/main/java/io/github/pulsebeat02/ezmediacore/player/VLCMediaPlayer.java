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
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.base.AudioApi;
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

  private static VideoSurfaceAdapter adapter;
  private static MinecraftVideoRenderCallback videoCallback;
  private static MinecraftAudioCallback audioCallback;
  private static CallbackVideoSurface surface;
  private static BufferFormatCallback bufferFormatCallback;

  private static MediaPlayerFactory factory;
  private static EmbeddedMediaPlayer player;
  private static NativeLog logger;

  VLCMediaPlayer(
      @NotNull final Callback callback,
      @NotNull final Viewers viewers,
      @NotNull final Dimension pixelDimension,
      @NotNull final MrlConfiguration url,
      @Nullable final SoundKey key,
      @NotNull final FrameConfiguration fps) {
    super(callback, viewers, pixelDimension, url, key, fps);
    this.adapter = this.getAdapter();
    this.videoCallback = new MinecraftVideoRenderCallback(this);
    this.initializePlayer(0L);
    this.setCustomVideoAdapter(this.getCallback()::process);
  }

  private VideoSurfaceAdapter getAdapter() {
    return switch (this.getCore().getDiagnostics().getSystem().getOSType()) {
      case MAC -> new OsxVideoSurfaceAdapter();
      case UNIX -> new LinuxVideoSurfaceAdapter();
      case WINDOWS -> new WindowsVideoSurfaceAdapter();
    };
  }

  @Override
  public void setPlayerState(@NotNull final PlayerControls controls) {
    super.setPlayerState(controls);
    final MrlConfiguration configuration = this.getMrlConfiguration();
    switch (controls) {
      case START -> {
        if (this.player == null) {
          this.initializePlayer(0L);
        }
        this.player.media().play(configuration.getMrl());
        this.playAudio();
      }
      case PAUSE -> {
        this.stopAudio();
        this.player.controls().stop();
      }
      case RESUME -> {
        if (this.player == null) {
          this.initializePlayer(0L);
          this.player.media().play(configuration.getMrl());
        } else {
          this.player.controls().play();
        }
        this.playAudio();
      }
      case RELEASE -> this.releaseAll();
      default -> throw new IllegalArgumentException("Player state is invalid!");
    }
  }

  @Override
  public void initializePlayer(final long ms) {

    this.player = this.getEmbeddedMediaPlayer();
    this.player.media().prepare(this.getMrlConfiguration().getMrl());
    this.player.controls().setTime(ms);

    final AudioApi audio = this.player.audio();
    if (!audio.isMute()) {
      audio.setMute(true);
    }

  }

  @Override
  public long getElapsedMilliseconds() {
    return this.player.status().time();
  }

  private @NotNull EmbeddedMediaPlayer getEmbeddedMediaPlayer() {
    if (this.player == null || this.factory == null
        || this.logger == null) { // just in case something is null
      this.releaseAll();
      final int rate = this.getFrameConfiguration().getFps();
      this.factory = new MediaPlayerFactory(
          rate != 0 ? new String[]{"sout=\"#transcode{fps=%d}\"".formatted(rate), "--no-audio"}
              : new String[]{});
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
    if (bufferFormatCallback == null) {
      final Dimension dimension = VLCMediaPlayer.this.getDimensions();
      final BufferFormatCallback callback = new BufferFormatCallback() {
        @Override
        public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
          return new RV32BufferFormat(dimension.getWidth(), dimension.getHeight());
        }

        @Override
        public void allocatedBuffers(final ByteBuffer[] buffers) {
        }
      };
      this.bufferFormatCallback = callback;
    }
    return bufferFormatCallback;
  }

  public void setCustomVideoAdapter(@NotNull final Consumer<int[]> pixels) {
    this.videoCallback = new MinecraftVideoRenderCallback(this, pixels);
    this.player.videoSurface().set(this.getSurface());
  }

  public void setCustomAudioAdapter(@NotNull final Consumer<byte[]> audio, final int blockSize,
      @NotNull final String format, final int rate, final int channels) {
    this.audioCallback = new MinecraftAudioCallback(audio, blockSize);
    this.player.audio().callback(format, rate, channels, this.audioCallback);
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
    public Builder mrl(@NotNull final MrlConfiguration mrl) {
      super.mrl(mrl);
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
      return new VLCMediaPlayer(callback, callback.getWatchers(), this.getDims(),
          this.getMrl(),
          this.getKey(), this.getRate());
    }
  }
}
