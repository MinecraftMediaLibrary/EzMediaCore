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

package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import java.nio.ByteBuffer;

public interface AbstractDitherHolder {

  /**
   * Dithers the buffer using the given width.
   *
   * @param buffer data for the image
   * @param width units for the image
   */
  void dither(final int[] buffer, final int width);

  /**
   * Dithers the buffer into the ByteBuffer.
   *
   * @param buffer data for the image
   * @param width units for the image
   * @return ByteBuffer buffer for dithered image
   */
  ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width);

  /**
   * Gets the current dither setting.
   *
   * @return DitherSetting for the setting.
   */
  DitherSetting getSetting();
}
