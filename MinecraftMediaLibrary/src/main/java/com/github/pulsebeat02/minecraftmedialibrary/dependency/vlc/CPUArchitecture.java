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

import org.jetbrains.annotations.NotNull;

public enum CPUArchitecture {
  AMD64,
  AARCH64,
  ARMV7H,
  ARMHFP,
  ARMV7HL,
  ARMV7,
  EARNMV7HF,
  X86_64,
  I386,
  I586,
  I486;

  public static CPUArchitecture fromName(@NotNull final String name) {
    for (CPUArchitecture val : values()) {
      if (val.name().equalsIgnoreCase(name.toUpperCase())) {
        return val;
      }
    }
    return null;
  }
}
