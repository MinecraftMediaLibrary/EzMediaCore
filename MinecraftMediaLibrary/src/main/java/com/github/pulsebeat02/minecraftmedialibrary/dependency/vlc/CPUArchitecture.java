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
  /**
   * Amd 64 cpu architecture.
   */
  AMD64,
  /** Aarch 64 cpu architecture. */
  AARCH64,
  /** Armhf cpu architecture. */
  ARMHF,
  /** Armv 7 h cpu architecture. */
  ARMV7H,
  /** Armhfp cpu architecture. */
  ARMHFP,
  /** Armv 7 hl cpu architecture. */
  ARMV7HL,
  /** Armv 7 cpu architecture. */
  ARMV7,
  /** Arm 64 cpu architecture. */
  ARM64,
  /** Earnmv 7 hf cpu architecture. */
  EARNMV7HF,
  /** X 86 64 cpu architecture. */
  X86_64,
  /** 386 cpu architecture. */
  I386,
  /**
   * 586 cpu architecture.
   */
  I586,
  /**
   * 486 cpu architecture.
   */
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
      if (val.name().equalsIgnoreCase(name.toUpperCase())) {
        return val;
      }
    }
    return null;
  }
}
