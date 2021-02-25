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

    public static final String CPU_ARCH;
    public static final String OPERATING_SYSTEM;
    public static final boolean MAC;
    public static final boolean WINDOWS;
    public static final boolean LINUX;
    public static String URL;

    static {
        Logger.info("Detecting Operating System...");
        OPERATING_SYSTEM = getOperatingSystem().toLowerCase();
        CPU_ARCH = getCpuArchitecture();
        LINUX =
                OPERATING_SYSTEM.contains("nix")
                        || OPERATING_SYSTEM.contains("nux")
                        || OPERATING_SYSTEM.contains("aix");
        WINDOWS = OPERATING_SYSTEM.contains("win");
        MAC = OPERATING_SYSTEM.contains("mac");
        if (is64Architecture(OPERATING_SYSTEM)) {
            if (WINDOWS) {
                Logger.info("Detected Windows 64 Bit!");
                URL = "http://download.videolan.org/pub/videolan/vlc/last/win64/vlc-3.0.12-win64.zip";
            } else if (LINUX) {
                if (CPU_ARCH.contains("arm")) {
                    Logger.info("Detected Linux ARM 64 Bit!");
                } else {
                    Logger.info("Detected Linux AMD/Intel 64 Bit!");
                }
                URL = "LINUX";
            } else if (MAC) {
                if (!CPU_ARCH.contains("amd")) {
                    Logger.info("Detected MACOS 64 Bit! (Silicon)");
                    URL =
                            "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-intel64/VLC.zip";
                } else {
                    Logger.info("Detected MACOS 64 Bit! (AMD)");
                    URL = " https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-arm64/VLC.zip";
                }
            }
    } else {
            if (WINDOWS) {
                URL = "http://download.videolan.org/pub/videolan/vlc/last/win32/vlc-3.0.12-win32.zip";
            } else if (LINUX) {
                if (CPU_ARCH.contains("arm")) {
                    Logger.info("Detected ARM 32 Bit!");
                    URL = "LINUX";
                }
            }
    }
    Logger.info("=========================================");
    Logger.info(" Final Results After Runtime Scanning... ");
    Logger.info("=========================================");
    Logger.info("Operating System: " + OPERATING_SYSTEM);
    Logger.info("CPU Architecture: " + CPU_ARCH);
    Logger.info("Link Used: " + URL);
    Logger.info("=========================================");
  }

  public static String getOperatingSystem() {
    return System.getProperty("os.name");
  }

  public static String getCpuArchitecture() {
    final String arch = System.getenv("PROCESSOR_ARCHITECTURE");
    final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
    return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64")
        ? "64"
        : "32";
  }

  private static boolean is64Architecture(@NotNull final String os) {
    if (os.contains("Windows")) {
      return System.getenv("ProgramFiles(x86)") != null;
    } else {
      return System.getProperty("os.arch").contains("64");
    }
  }
}
