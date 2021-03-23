/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/19/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;

public class JavaVersionUtilities {

  public static final String JAVA_VERSION;
  public static final int MAJOR_VERSION;

  static {
    JAVA_VERSION = System.getProperty("java.version");
    MAJOR_VERSION = Integer.parseInt(JAVA_VERSION.split("\\.")[1]);
  }

  /**
   * Gets Java Version.
   *
   * @return Java Version
   */
  public static String getJavaVersion() {
    return JAVA_VERSION;
  }

  /**
   * Gets Java Major Version.
   *
   * @return Major Java Version
   */
  public static int getMajorVersion() {
    return MAJOR_VERSION;
  }

  public void sendWarningMessage() {
    if (MAJOR_VERSION < 11) {
      Logger.warn(
          "MinecraftMediaPlugin is moving towards a newer Java Version (Java 11) \n"
              + "Please switch as soon as possible before the library will be incompatible \n"
              + "with your server. If you want to read more information surrounding this, \n"
              + "you may want to take a look here at "
              + "https://papermc.io/forums/t/java-11-mc-1-17-and-paper/5615");
    }
  }
}
