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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.OperatingSystemUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ZipFileUtilities;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class VLCNativeDependencyFetcher {

  public Plugin plugin;

  public VLCNativeDependencyFetcher(@NotNull final Plugin plugin) {
    this.plugin = plugin;
  }

  public void downloadLibraries() {
    boolean installed = new NativeDiscovery().discover();
    if (!installed) {
      String option = OperatingSystemUtilities.DOWNLOAD_OPTION;
      if (option.equalsIgnoreCase("COMPILE")) {

      } else {
        // Download Zip from hosted repo
        try {
          File zip = new File("VLC.zip");
          FileUtils.copyURLToFile(new URL(option), zip);
          String path = zip.getAbsolutePath();
          String dest = zip.getParent() + "/libs/vlcj";
          // Extract to libs folder
          ZipFileUtilities.unzip(path, dest);
          if (zip.delete()) {
            Logger.info("VLC zip deleted after installation.");
          } else {
            Logger.error("VLC zip could NOT be deleted after installation!");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
