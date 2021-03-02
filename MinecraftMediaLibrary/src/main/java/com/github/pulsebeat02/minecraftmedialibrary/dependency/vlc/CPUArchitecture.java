/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/23/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum CPUArchitecture {

  /** AMD64 CPU Architecture. */
  AMD64,

  /** AARCH64 CPU Architecture. */
  AARCH64,

  /** ARMHF CPU Architecture. */
  ARMHF,

  /** ARMV7H CPU Architecture. */
  ARMV7H,

  /** ARMHFP CPU Architecture. */
  ARMHFP,

  /** ARMV7HL CPU Architecture. */
  ARMV7HL,

  /** ARMV7 CPU Architecture. */
  ARMV7,

  /** ARM64 CPU Architecture. */
  ARM64,

  /** EARNMV7HF CPU Architecture. */
  EARNMV7HF,

  /** X86_64 CPU Architecture. */
  X86_64,

  /** I386 CPU Architecture. */
  I386,

  /** I586 CPU Architecture. */
  I586,

  /** I486 CPU Architecture. */
  I486;

  static {
    Logger.info("Listing All Possible CPU Architectures...");
    Arrays.stream(values()).forEach(x -> Logger.info(x.name()));
  }

  /**
   * From name cpu architecture.
   *
   * @param name the name
   * @return the cpu architecture
   */
  public static CPUArchitecture fromName(@NotNull final String name) {
    for (final CPUArchitecture val : values()) {
      if (val.name().equalsIgnoreCase(name)) {
        return val;
      }
    }
    return null;
  }
}
