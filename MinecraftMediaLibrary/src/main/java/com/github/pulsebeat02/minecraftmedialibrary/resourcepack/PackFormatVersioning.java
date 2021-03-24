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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

public enum PackFormatVersioning {

  /** 1.15 pack format versioning. */
  VER_1_15(5),

  /** 1.15.1 Pack Format */
  VER_1_15_1(5),

  /** 1.15.2 Pack Format */
  VER_1_15_2(5),

  /** 1.16.1 Pack Format */
  VER_1_16_1(5),

  /** 1.16.2 Pack Format */
  VER_1_16_2(6),

  /** 1.16.3 Pack Format */
  VER_1_16_3(6),

  /** 1.16.4 Pack Format */
  VER_1_16_4(6),

  /** 1.16.5 Pack Format */
  VER_1_16_5(6);

  private final int packFormat;

  /**
   * Instantiates a pack format.
   * @param id format id
   */
  PackFormatVersioning(final int id) {
    packFormat = id;
  }

  /**
   * Gets pack format id.
   *
   * @return the pack format id
   */
  public int getPackFormatID() {
    return packFormat;
  }
}
