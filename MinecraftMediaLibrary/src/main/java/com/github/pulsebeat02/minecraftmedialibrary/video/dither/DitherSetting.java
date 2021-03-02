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

/** The enum Dither setting. */
public enum DitherSetting {

  /** Standard Minecraft Dithering */
  STANDARD_MINECRAFT_DITHER(new StandardDithering()),

  /** Sierra Filter Lite Dithering */
  SIERRA_FILTER_LITE_DITHER(new FilterLiteDither()),

  /** Bayer Ordered 2 Dimensional Dithering */
  BAYER_ORDERED_2_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeTwo)),

  /** Bayer Ordered 4 Dimensional Dithering */
  BAYER_ORDERED_4_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeFour)),

  /** Bayer Ordered 8 Dimensional Dithering */
  BAYER_ORDERED_8_DIMENSIONAL(new OrderedDithering(OrderedDithering.DitherType.ModeEight)),

  /** Floyd Steinberg Dithering */
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
