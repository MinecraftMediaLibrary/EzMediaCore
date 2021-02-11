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

package com.github.pulsebeat02.minecraftmedialibrary.video.itemframe;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.AbstractDitherHolder;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ItemFrameCallback implements AbstractCallback {

  private final MinecraftMediaLibrary library;
  private final UUID[] viewers;
  private final AbstractDitherHolder type;
  private final long map;
  private final int videoWidth;
  private final int delay;
  private int width;
  private int height;
  private long lastUpdated;

  public ItemFrameCallback(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final UUID[] viewers,
      final long map,
      final int width,
      final int height,
      final int videoWidth,
      final int delay,
      @NotNull final AbstractDitherHolder type) {
    this.library = library;
    this.viewers = viewers;
    this.type = type;
    this.map = map;
    this.width = width;
    this.height = height;
    this.videoWidth = videoWidth;
    this.delay = delay;
  }

  public UUID[] getViewers() {
    return viewers;
  }

  public long getMap() {
    return map;
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

  public int getDelay() {
    return delay;
  }

  @Override
  public void send(final int[] data) {
    final long time = System.currentTimeMillis();
    final long difference = time - lastUpdated;
    if (difference >= delay) {
      lastUpdated = time;
      final ByteBuffer dithered = type.ditherIntoMinecraft(data, videoWidth);
      library.getHandler().display(viewers, map, width, height, dithered, videoWidth);
    }
  }

  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public long getLastUpdated() {
    return lastUpdated;
  }

  public AbstractDitherHolder getType() {
    return type;
  }

  public class Builder {

    private UUID[] viewers;
    private AbstractDitherHolder type;
    private int map;
    private int width;
    private int height;
    private int videoWidth;
    private int delay;

    public Builder setViewers(final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    public Builder setMap(final int map) {
      this.map = map;
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

    public Builder setVideoWidth(final int videoWidth) {
      this.videoWidth = videoWidth;
      return this;
    }

    public Builder setDelay(final int delay) {
      this.delay = delay;
      return this;
    }

    public Builder setDitherHolder(final AbstractDitherHolder holder) {
      this.type = holder;
      return this;
    }

    public ItemFrameCallback createItemFrameCallback(final MinecraftMediaLibrary library) {
      return new ItemFrameCallback(library, viewers, map, width, height, videoWidth, delay, type);
    }
  }
}
