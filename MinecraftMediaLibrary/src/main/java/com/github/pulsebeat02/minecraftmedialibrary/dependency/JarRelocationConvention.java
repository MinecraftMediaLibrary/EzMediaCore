package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import org.jetbrains.annotations.NotNull;

public enum JarRelocationConvention {

  /** VLCJ Jar Relocation Convention */
  VLCJ(RepositoryDependency.VLCJ, "com.github.pulsebeat02.vlcj"),

  /** VLCJ Natives Jar Relocation Convention */
  VLCJ_NATIVES(RepositoryDependency.VLCJ_NATIVES, "com.github.pulsebeat02.vlcj.bindings"),

  /** Youtube Downloader Jar Relocation Convention */
  YOUTUBE_DOWNLOADER(RepositoryDependency.YOUTUBE_DOWNLOADER, "com.github.pulsebeat02.youtube"),

  /** Jave Core Jar Relocation Convention */
  JAVE_CORE(RepositoryDependency.JAVE_CORE, "com.github.pulsebeat02.jave"),

  /** Compression Jar Relocation Convention */
  COMPRESSION(RepositoryDependency.COMPRESSION, "com.github.pulsebeat02.jarchivelib"),

  /** ASM Jar Relocation Convention */
  ASM(RepositoryDependency.ASM, "com.github.pulsebeat02.asm"),

  /** ASM Commons Jar Relocation Convention */
  ASM_COMMONS(RepositoryDependency.ASM_COMMONS, "com.github.pulsebeat02.asm.commons");

  private final RepositoryDependency dependency;
  private final String name;

  JarRelocationConvention(
      @NotNull final RepositoryDependency dependency, @NotNull final String name) {
    this.dependency = dependency;
    this.name = name;
  }

  /**
   * Gets dependency.
   *
   * @return the dependency
   */
  public RepositoryDependency getDependency() {
    return dependency;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }
}
