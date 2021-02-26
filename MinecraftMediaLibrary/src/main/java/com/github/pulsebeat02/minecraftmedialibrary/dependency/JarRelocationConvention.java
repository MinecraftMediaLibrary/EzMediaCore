package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.relocation.Relocation;
import org.jetbrains.annotations.NotNull;

public enum JarRelocationConvention {

  /**
   * VLCJ Jar Relocation Convention
   */
  VLCJ("uk.co.caprica.vlcj", "com.github.pulsebeat02.vlcj"),

    /** VLCJ Natives Jar Relocation Convention */
  VLCJ_NATIVES("uk.co.caprica.vlcj.binding", "com.github.pulsebeat02.vlcj.binding"),

    /** Youtube Downloader Jar Relocation Convention */
  YOUTUBE_DOWNLOADER("com.github.sealedtx.downloader", "com.github.pulsebeat02.youtube"),

    /** Jave Core Jar Relocation Convention */
  JAVE_CORE("ws.schild.jave", "com.github.pulsebeat02.jave"),

    /** Compression Jar Relocation Convention */
  COMPRESSION("org.rauschig.jarchivelib", "com.github.pulsebeat02.jarchivelib"),

    /** ASM Jar Relocation Convention */
  ASM("org.ow2.asm", "com.github.pulsebeat02.asm"),

    /** ASM Commons Jar Relocation Convention */
  ASM_COMMONS("org.ow2.asm.commons", "com.github.pulsebeat02.asm.commons");

  private final Relocation relocation;

    JarRelocationConvention(@NotNull final String before, @NotNull final String after) {
        relocation = new Relocation(before, after);
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
