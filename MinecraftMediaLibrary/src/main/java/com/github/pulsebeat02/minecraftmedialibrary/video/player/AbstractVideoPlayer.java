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
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractVideoPlayer {

  private final MinecraftMediaLibrary library;
  private final String url;
  private final Consumer<int[]> callback;
  private int width;
  private int height;

  /**
   * Instantiates a new Abstract video player.
   *
   * @param library  the library
   * @param url      the url
   * @param width    the width
   * @param height   the height
   * @param callback the callback
   */
  public AbstractVideoPlayer(
          @NotNull final MinecraftMediaLibrary library,
          @NotNull final String url,
          final int width,
          final int height,
          @NotNull final Consumer<int[]> callback) {
    this.library = library;
    this.url = url;
    this.width = width;
    this.height = height;
    this.callback = callback;
  }

  /**
   * Gets library.
   *
   * @return the library
   */
  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(final int width) {
    this.width = width;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(final int height) {
    this.height = height;
  }

  /**
   * Gets callback.
   *
   * @return the callback
   */
  public Consumer<int[]> getCallback() {
    return callback;
  }

  /**
   * Start.
   */
  public abstract void start();

  /**
   * Stop.
   */
  public abstract void stop();
}
