/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

public class OperatingSystemUtilities {

  public static String DOWNLOAD_OPTION;

  static {
    Logger.info("Detecting Operating System...");
    final String os = System.getProperty("os.name").toLowerCase();
    final boolean linux = os.contains("nix") || os.contains("nux") || os.contains("aix");
    final boolean windows = os.contains("win");
    if (is64Architecture(os)) {
      if (windows) {
        Logger.info("Detected Windows 64 Bit!");
        DOWNLOAD_OPTION = "http://download.videolan.org/pub/videolan/vlc/last/win64/vlc-3.0.12-win64.zip";
      } else if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected Linux ARM 64 Bit!");
          DOWNLOAD_OPTION = "COMPILE";
        } else {
          Logger.info("Detected Linux AMD/Intel 64 Bit!");
          DOWNLOAD_OPTION = "COMPILE";
        }
      } else if (os.contains("mac")) {
        if (!getCpuArchitecture().contains("amd")) {
          Logger.info("Detected MACOS 64 Bit! (Silicon)");
          DOWNLOAD_OPTION = "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-intel64/VLC.zip";
        } else {
          Logger.info("Detected MACOS 64 Bit! (AMD)");
          DOWNLOAD_OPTION = " https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-arm64/VLC.zip";
        }
      }
    } else {
      if (windows) {
        DOWNLOAD_OPTION = "http://download.videolan.org/pub/videolan/vlc/last/win32/vlc-3.0.12-win32.zip";
      } else if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected ARM 32 Bit!");
          DOWNLOAD_OPTION = "COMPILE";
        }
      }
    }
  }

  private static String getCpuArchitecture() {
    String arch = System.getenv("PROCESSOR_ARCHITECTURE");
    String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
    return arch != null && arch.endsWith("64")
            || wow64Arch != null && wow64Arch.endsWith("64")
            ? "64" : "32";
  }

  private static boolean is64Architecture(@NotNull final String os) {
    if (os.contains("Windows")) {
      return System.getenv("ProgramFiles(x86)") != null;
    } else {
      return System.getProperty("os.arch").contains("64");
    }
  }
}
