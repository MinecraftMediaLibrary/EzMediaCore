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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import org.jetbrains.annotations.NotNull;

public class LinuxPackage {

  private final CPUArchitecture arch;
  private final String url;
  private final String mirror;

  /**
   * Instantiates a new LinuxPackage.
   *
   * @param url the url
   * @param arch the arch
   */
  public LinuxPackage(@NotNull final String url, @NotNull final CPUArchitecture arch) {
    this.arch = arch;
    this.url = url;
    mirror =
        "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/linux/"
            + url.substring(url.lastIndexOf("/") + 1);
  }

  /**
   * Instantiates a new LinuxPackage.
   *
   * @param url the url
   * @param mirror the mirror url
   * @param arch the arch
   */
  public LinuxPackage(
      @NotNull final String url,
      @NotNull final String mirror,
      @NotNull final CPUArchitecture arch) {
    this.arch = arch;
    this.url = url;
    this.mirror = mirror;
  }

  /**
   * Gets CPU Architecture.
   *
   * @return CPU Architecture of package
   */
  public CPUArchitecture getArch() {
    return arch;
  }

  /**
   * Gets the package URL.
   *
   * @return the package URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the mirror URL.
   *
   * @return the mirror URL
   */
  public String getMirror() {
    return mirror;
  }
}
