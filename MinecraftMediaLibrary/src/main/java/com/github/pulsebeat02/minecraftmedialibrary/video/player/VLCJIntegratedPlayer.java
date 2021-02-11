/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.video.player;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class VLCJIntegratedPlayer extends AbstractVideoPlayer {

  private final EmbeddedMediaPlayer mediaPlayerComponent;

  // youtube url
  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, url, width, height, callback);
    this.mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
    final BufferFormatCallback bufferFormatCallback =
        new BufferFormatCallback() {
          @Override
          public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
            return new RV32BufferFormat(getWidth(), getHeight());
          }

          @Override
          public void allocatedBuffers(final ByteBuffer[] buffers) {}
        };
    final CallbackVideoSurface surface =
        new CallbackVideoSurface(
            bufferFormatCallback,
            new MinecraftRenderCallback(),
            false,
            new WindowsVideoSurfaceAdapter());
    mediaPlayerComponent.videoSurface().set(surface);
    Logger.info("Created a VLCJ Integrated Video Player (" + url + ")");
  }

  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final File file,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, file.getAbsolutePath(), width, height, callback);
    this.mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
    final BufferFormatCallback bufferFormatCallback =
        new BufferFormatCallback() {
          @Override
          public BufferFormat getBufferFormat(final int sourceWidth, final int sourceHeight) {
            return new RV32BufferFormat(getWidth(), getHeight());
          }

          @Override
          public void allocatedBuffers(final ByteBuffer[] buffers) {}
        };
    final CallbackVideoSurface surface =
        new CallbackVideoSurface(
            bufferFormatCallback,
            new MinecraftRenderCallback(),
            false,
            new WindowsVideoSurfaceAdapter());
    mediaPlayerComponent.videoSurface().set(surface);
    Logger.info("Created a VLCJ Integrated Video Player (" + file.getAbsolutePath() + ")");
  }

  @Override
  public void start() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.release();
    }
    final String url = getUrl();
    mediaPlayerComponent.media().play(url);
    Logger.info("Started Playing Video! (" + url + ")");
  }

  @Override
  public void stop() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.controls().stop();
      Logger.info("Stopped Playing Video! (" + getUrl() + ")");
    }
  }

  public EmbeddedMediaPlayer getMediaPlayerComponent() {
    return mediaPlayerComponent;
  }

  private class MinecraftRenderCallback extends RenderCallbackAdapter {

    private MinecraftRenderCallback() {
      super(new int[getWidth() * getHeight()]);
    }

    @Override
    protected void onDisplay(final MediaPlayer mediaPlayer, final int[] buffer) {
      getCallback().accept(buffer);
    }
  }

  public class Builder {

    private String url;
    private int width;
    private int height;
    private Consumer<int[]> callback;

    public Builder setUrl(@NotNull final String url) {
      this.url = url;
      return this;
    }

    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    public Builder setCallback(@NotNull final Consumer<int[]> callback) {
      this.callback = callback;
      return this;
    }

    public VLCJIntegratedPlayer createVLCJIntegratedPlayer(
        @NotNull final MinecraftMediaLibrary library) {
      return new VLCJIntegratedPlayer(library, url, width, height, callback);
    }
  }
}
