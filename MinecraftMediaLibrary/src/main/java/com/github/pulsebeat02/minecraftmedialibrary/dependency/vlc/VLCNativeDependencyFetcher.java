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
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.MacSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.WindowsSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class VLCNativeDependencyFetcher {

  /** Instantiates a new VLCNativeDependencyFetcher. */
  private final String dir;

  public VLCNativeDependencyFetcher(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
  }

  public VLCNativeDependencyFetcher(@NotNull final String dir) {
    this.dir = dir;
  }

  /**
   * Download libraries.
   *
   * Currently in progress! Not finished as I am trying to support other operating systems.
   *
   */
  public void downloadLibraries() {
    Logger.info("Trying to find Native VLC Installation...");
    try {
      if (RuntimeUtilities.isLINUX()) {

      } else if (RuntimeUtilities.isWINDOWS()) {
        new WindowsSilentInstallation(dir).downloadVLCLibrary();
      } else if (RuntimeUtilities.isMAC()) {
        new MacSilentInstallation(dir).downloadVLCLibrary();
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
