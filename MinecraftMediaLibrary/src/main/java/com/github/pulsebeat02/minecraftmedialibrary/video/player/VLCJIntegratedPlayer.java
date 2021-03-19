/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
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

  /**
   * Instantiates a new Vlcj integrated player.
   *
   * @param library the library
   * @param url the url
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final String url,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, url, width, height, callback);
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
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

  /**
   * Instantiates a new VLCJIntegratedPlayer.
   *
   * @param library the library
   * @param file the file
   * @param width the width
   * @param height the height
   * @param callback the callback
   */
  public VLCJIntegratedPlayer(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final File file,
      final int width,
      final int height,
      @NotNull final Consumer<int[]> callback) {
    super(library, file.getAbsolutePath(), width, height, callback);
    mediaPlayerComponent = new MediaPlayerFactory().mediaPlayers().newEmbeddedMediaPlayer();
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

  /** Starts playing the video. */
  @Override
  public void start() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.release();
    }
    final String url = getUrl();
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.media().play(url);
    }
    Logger.info("Started Playing Video! (" + url + ")");
  }

  /** Stops playing the video. */
  @Override
  public void stop() {
    if (mediaPlayerComponent != null) {
      mediaPlayerComponent.controls().stop();
      Logger.info("Stopped Playing Video! (" + getUrl() + ")");
    }
  }

  /**
   * Gets media player component.
   *
   * @return the media player component
   */
  public EmbeddedMediaPlayer getMediaPlayerComponent() {
    return mediaPlayerComponent;
  }

  /** The type Builder. */
  public static class Builder {

    private String url;
    private int width;
    private int height;
    private Consumer<int[]> callback;

    /**
     * Sets url.
     *
     * @param url the url
     * @return the url
     */
    public Builder setUrl(@NotNull final String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets callback.
     *
     * @param callback the callback
     * @return the callback
     */
    public Builder setCallback(@NotNull final Consumer<int[]> callback) {
      this.callback = callback;
      return this;
    }

    /**
     * Create vlcj integrated player vlcj integrated player.
     *
     * @param library the library
     * @return the vlcj integrated player
     */
    public VLCJIntegratedPlayer createVLCJIntegratedPlayer(
        @NotNull final MinecraftMediaLibrary library) {
      return new VLCJIntegratedPlayer(library, url, width, height, callback);
    }
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
}
