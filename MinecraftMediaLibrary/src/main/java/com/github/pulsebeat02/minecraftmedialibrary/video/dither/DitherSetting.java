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

import org.jetbrains.annotations.NotNull;

public enum DitherSetting {
  /**
   * The Standard minecraft dither.
   */
  STANDARD_MINECRAFT_DITHER(new StandardDithering()),
  /** The Sierra filter lite dither. */
  SIERRA_FILTER_LITE_DITHER(new FilterLiteDither()),
  /** The Bayer ordered 2 dimensional. */
  BAYER_ORDERED_2_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeTwo)),
  /** The Bayer ordered 4 dimensional. */
  BAYER_ORDERED_4_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeFour)),
  /** The Bayer ordered 8 dimensional. */
  BAYER_ORDERED_8_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeEight)),
  /**
   * The Floyd steinberg dither.
   */
  FLOYD_STEINBERG_DITHER(new FloydImageDither());

  private final AbstractDitherHolder holder;

  DitherSetting(@NotNull final AbstractDitherHolder holder) {
    this.holder = holder;
  }

  /**
   * Gets holder.
   *
   * @return the holder
   */
  public AbstractDitherHolder getHolder() {
    return holder;
  }
}
