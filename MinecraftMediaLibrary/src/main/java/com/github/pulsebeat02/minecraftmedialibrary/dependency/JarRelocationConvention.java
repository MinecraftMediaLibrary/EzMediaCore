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

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.relocation.Relocation;
import org.jetbrains.annotations.NotNull;

public enum JarRelocationConvention {

  /** VLCJ Jar Relocation Convention */
  VLCJ("uk{}co{}caprica{}vlcj", "com{}github{}pulsebeat02{}vlcj"),

  /** VLCJ Natives Jar Relocation Convention */
  VLCJ_NATIVES("uk{}co{}caprica{}vlcj{}binding", "com{}github{}pulsebeat02{}vlcj{}binding"),

  /** Youtube Downloader Jar Relocation Convention */
  YOUTUBE_DOWNLOADER("com{}github{}sealedtx{}downloader", "com{}github{}pulsebeat02{}youtube"),

  /** Jave Core Jar Relocation Convention */
  JAVE_CORE("ws{}schild{}jave", "com{}github{}pulsebeat02{}jave"),

  /** Apache Commons Compression Jar Relocation Convention */
  COMMONS_COMPRESSION("org{}apache{}commons{}compress", "com{}github{}pulsebeat02{}compress"),

  /** Compression Jar Relocation Convention */
  COMPRESSION("org{}rauschig{}jarchivelib", "com{}github{}pulsebeat02{}jarchivelib"),

  /** Compress XZ Jar Relocation Convention */
  XZ("org{}tukaani.xz", "com{}github{}pulsebeat02{}xz"),

  /** ASM Jar Relocation Convention */
  ASM("org{}ow2{}asm", "com{}github{}pulsebeat02{}asm"),

  /** ASM Commons Jar Relocation Convention */
  ASM_COMMONS("org{}ow2{}asm{}commons", "com{}github{}pulsebeat02{}asm{}commons");

  //  /** JNA Jar Relocation Convention */
  //  JNA("com{}sun{}jna", "com{}github{}pulsebeat02{}jna");

  private final Relocation relocation;

  JarRelocationConvention(@NotNull final String before, @NotNull final String after) {
    relocation = new Relocation(before.replaceAll("\\{}", "."), after.replaceAll("\\{}", "."));
  }

  /**
   * Gets relocation.
   *
   * @return the relocation
   */
  public Relocation getRelocation() {
    return relocation;
  }
}
