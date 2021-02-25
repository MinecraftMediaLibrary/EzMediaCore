/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackFormatVersioning;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResourcepackUtilities {

  /**
   * Validate pack format.
   *
   * @param format the format
   * @return pack format validation
   */
  public static boolean validatePackFormat(final int format) {
    for (final PackFormatVersioning version : PackFormatVersioning.values()) {
      if (format == version.getPackFormatID()) {
        Logger.info("Pack Format Supported! (" + format + ")");
        return true;
      }
    }
    Logger.warn("Pack Format Not Supported! (" + format + ")");
    return false;
  }

  /**
   * Validate resourcepack icon.
   *
   * @param icon the icon
   * @return resourcepack icon validation
   */
  public static boolean validateResourcepackIcon(@NotNull final File icon) {
    final boolean valid = icon.getName().endsWith(".png");
    if (valid) {
      Logger.info("Resourcepack Icon Accepted! (" + icon.getAbsolutePath() + ")");
    } else {
      Logger.warn("Resourcepack Icon Not Supported! (" + icon.getAbsolutePath() + ")");
    }
    return icon.getName().endsWith(".png");
  }
}
