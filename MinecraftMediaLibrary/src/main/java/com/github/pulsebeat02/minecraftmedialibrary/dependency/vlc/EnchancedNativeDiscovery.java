/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/24/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.utility.OperatingSystemUtilities;
// import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.io.File;

public class EnchancedNativeDiscovery implements NativeDiscoveryStrategy {

  /** PLUGIN_ENV_NAME stores the System Enviornment Variable */
  protected static final String PLUGIN_ENV_NAME = "VLC_PLUGIN_PATH";

  private static final String[] PLUGIN_PATH_FORMATS = {
    "\\plugins", "\\vlc\\plugins", "/../plugins", "/plugins", "/vlc/plugins",
  };

  private static String path;

  /** Returns whether the strategy is supported */
  @Override
  public boolean supported() {
    return true;
  }

  /**
   * Attempts to discover VLC installation downloaded from pre-compiled binaries.
   *
   * @return String discovered path, null if not found.
   */
  @Override
  public String discover() {
    final String folder = OperatingSystemUtilities.MAC ? "\\vlc" : "/vlc";
    for (final String str : PLUGIN_PATH_FORMATS) {
      final File f = new File(folder, str);
      if (f.exists()) {
        path = f.getAbsolutePath();
        return path;
      }
    }
    return null;
  }

  /**
   * Ran once path is found.
   *
   * @param s path
   * @return found
   */
  @Override
  public boolean onFound(final String s) {
    return true;
  }

  /**
   * Ran once plugin path is set.
   *
   * @param s path
   * @return found
   */
  @Override
  public boolean onSetPluginPath(final String s) {
    // TODO: Fix this and set library path correctly. Currently, class is compiled later on than its supposed to be
    // return LibC.INSTANCE.setenv(PLUGIN_ENV_NAME, path, 1) == 0;
    return false;
  }
}
