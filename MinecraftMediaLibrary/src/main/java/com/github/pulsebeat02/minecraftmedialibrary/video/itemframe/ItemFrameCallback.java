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
  private final int map;
  private final int videoWidth;
  private final int delay;
  private int width;
  private int height;
  private long lastUpdated;

  /**
   * Instantiates a new Item frame callback.
   *
   * @param library the library
   * @param viewers the viewers
   * @param map the map
   * @param width the width
   * @param height the height
   * @param videoWidth the video width
   * @param delay the delay
   * @param type the type
   */
  public ItemFrameCallback(
      @NotNull final MinecraftMediaLibrary library,
      @NotNull final UUID[] viewers,
      final int map,
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

  /**
   * Get viewers uuid [ ].
   *
   * @return the uuid [ ]
   */
  public UUID[] getViewers() {
    return viewers;
  }

  /**
   * Gets map.
   *
   * @return the map
   */
  public long getMap() {
    return map;
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
   * Gets delay.
   *
   * @return the delay
   */
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

  /**
   * Gets library.
   *
   * @return the library
   */
  public MinecraftMediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets video width.
   *
   * @return the video width
   */
  public int getVideoWidth() {
    return videoWidth;
  }

  /**
   * Gets last updated.
   *
   * @return the last updated
   */
  public long getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public AbstractDitherHolder getType() {
    return type;
  }

  /** The type Builder. */
  public class Builder {

    private UUID[] viewers;
    private AbstractDitherHolder type;
    private int map;
    private int width;
    private int height;
    private int videoWidth;
    private int delay;

    /**
     * Sets viewers.
     *
     * @param viewers the viewers
     * @return the viewers
     */
    public Builder setViewers(final UUID[] viewers) {
      this.viewers = viewers;
      return this;
    }

    /**
     * Sets map.
     *
     * @param map the map
     * @return the map
     */
    public Builder setMap(final int map) {
      this.map = map;
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
     * Sets video width.
     *
     * @param videoWidth the video width
     * @return the video width
     */
    public Builder setVideoWidth(final int videoWidth) {
      this.videoWidth = videoWidth;
      return this;
    }

    /**
     * Sets delay.
     *
     * @param delay the delay
     * @return the delay
     */
    public Builder setDelay(final int delay) {
      this.delay = delay;
      return this;
    }

    /**
     * Sets dither holder.
     *
     * @param holder the holder
     * @return the dither holder
     */
    public Builder setDitherHolder(final AbstractDitherHolder holder) {
      type = holder;
      return this;
    }

    /**
     * Create item frame callback item frame callback.
     *
     * @param library the library
     * @return the item frame callback
     */
    public ItemFrameCallback createItemFrameCallback(final MinecraftMediaLibrary library) {
      return new ItemFrameCallback(library, viewers, map, width, height, videoWidth, delay, type);
    }
  }
}
