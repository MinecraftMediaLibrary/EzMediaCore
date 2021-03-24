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

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.LinuxSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.MacSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.WindowsSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * The main class of the VLC native dependency fetcher. It calls the correct classes and dependency
 * solution based on the working operating system of the environment. It should be noted that VLC
 * uses operating system dependent binaries, meaning that each operating system must be carefully
 * handled correctly in order to load the binaries properly.
 */
public class VLCNativeDependencyFetcher {

  /** Instantiates a new VLCNativeDependencyFetcher. */
  private final String dir;

  /**
   * Instantiates a new VLC Native Dependency Fetcher process.
   *
   * @param library the library
   */
  public VLCNativeDependencyFetcher(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
  }

  /**
   * Instantiates a new VLC Native Dependency Fetcher process.
   *
   * @param dir the directory
   */
  public VLCNativeDependencyFetcher(@NotNull final String dir) {
    this.dir = dir;
  }

  /**
   * Download libraries.
   *
   * <p>Currently in progress! Not finished as I am trying to support other operating systems.
   */
  public void downloadLibraries() {
    Logger.info("Trying to find Native VLC Installation...");
    try {
      if (RuntimeUtilities.isLinux()) {
        new LinuxSilentInstallation(dir).downloadVLCLibrary();
      } else if (RuntimeUtilities.isWindows()) {
        new WindowsSilentInstallation(dir).downloadVLCLibrary();
      } else if (RuntimeUtilities.isMac()) {
        new MacSilentInstallation(dir).downloadVLCLibrary();
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
