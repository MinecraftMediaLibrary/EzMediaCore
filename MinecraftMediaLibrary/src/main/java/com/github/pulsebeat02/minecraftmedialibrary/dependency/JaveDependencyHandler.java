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

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

// Not used anymore

@Deprecated
public class JaveDependencyHandler {

  private final String path;

  public JaveDependencyHandler() {
    this.path = System.getProperty("user.dir");
  }

  public File installAndLoad() {
    for (final File f : new File(path + "/mml_libs").listFiles()) {
      if (f.getName().contains("jave")) {
        try {
          DependencyUtilities.loadDependency(f);
        } catch (final IOException e) {
          e.printStackTrace();
        }
        return null;
      }
    }
    File file = null;
    try {
      file =
          DependencyUtilities.downloadFile(
              "ws.schild", getArtifactId(), "2.7.3", path + "/mml_libs");
    } catch (final IOException e) {
      e.printStackTrace();
    }
    try {
      if (file != null) {
        DependencyUtilities.loadDependency(file);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return file;
  }

  public String getArtifactId() {
    Logger.info("Detecting Operating System...");
    final String os = System.getProperty("os.name").toLowerCase();
    String artifactId = "INVALID_OPERATING_SYSTEM";
    final boolean linux = os.contains("nix") || os.contains("nux") || os.contains("aix");
    if (is64Architecture(os)) {
      if (os.contains("win")) {
        Logger.info("Detected Windows 64 Bit!");
        artifactId = "jave-nativebin-win64";
      } else if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected Linux ARM 64 Bit!");
          artifactId = "jave-nativebin-linux-arm64";
        } else {
          Logger.info("Detected Linux AMD/Intel 64 Bit!");
          artifactId = "jave-nativebin-linux64";
        }
      } else if (os.contains("mac")) {
        Logger.info("Detected MACOS 64 Bit!");
        artifactId = "jave-nativebin-osx64";
      }
    } else {
      if (linux) {
        if (os.contains("arm")) {
          Logger.info("Detected ARM 32 Bit!");
          artifactId = "jave-nativebin-linux-arm32";
        }
      }
    }
    return artifactId;
  }

  public boolean is64Architecture(@NotNull final String os) {
    if (os.contains("Windows")) {
      return System.getenv("ProgramFiles(x86)") != null;
    } else {
      return System.getProperty("os.arch").contains("64");
    }
  }
}
