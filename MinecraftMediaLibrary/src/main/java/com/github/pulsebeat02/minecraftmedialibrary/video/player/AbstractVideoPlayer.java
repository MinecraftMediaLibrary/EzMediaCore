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

  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  public String getUrl() {
    return url;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public Consumer<int[]> getCallback() {
    return callback;
  }

  public abstract void start();

  public abstract void stop();
}
