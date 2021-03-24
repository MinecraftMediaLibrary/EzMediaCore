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

package com.github.pulsebeat02.minecraftmedialibrary.concurrent;

import com.github.pulsebeat02.minecraftmedialibrary.image.ImageMapHolder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An async helper class used for image map drawing.
 */
public class AsyncImageMapDrawer {

  private final ImageMapHolder imageMapHolder;

  /**
   * Instantiates a new AsyncImageMapDrawer.
   *
   * @param imageMapHolder the image map holder
   */
  public AsyncImageMapDrawer(@NotNull final ImageMapHolder imageMapHolder) {
    this.imageMapHolder = imageMapHolder;
  }

  /**
   * Draw an image with CompletableFuture
   *
   * @return the CompletableFuture
   */
  public CompletableFuture<Void> drawImage() {
    return CompletableFuture.runAsync(imageMapHolder::drawImage);
  }
}
