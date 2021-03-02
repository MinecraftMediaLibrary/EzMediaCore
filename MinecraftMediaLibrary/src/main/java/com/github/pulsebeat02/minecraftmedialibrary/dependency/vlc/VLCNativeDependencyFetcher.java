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
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ZipFileUtilities;
import org.apache.commons.io.FileUtils;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class VLCNativeDependencyFetcher {

  /**
   * Instantiates a new VLCNativeDependencyFetcher.
   */
  public VLCNativeDependencyFetcher() {
  }

  /**
   * Download libraries.
   */
  public void downloadLibraries() {
    Logger.info("Trying to find Native VLC Installation...");
    final NativeDiscovery nativeDiscovery = new NativeDiscovery();
    final EnchancedNativeDiscovery enchancedNativeDiscovery = new EnchancedNativeDiscovery();
    final boolean installed =
            nativeDiscovery.discover() || enchancedNativeDiscovery.discover() != null;
    if (!installed) {
      Logger.info("No VLC Installation found on this system. Proceeding to install.");
      final String option = RuntimeUtilities.URL;
      if (option.equalsIgnoreCase("LINUX")) {
        try {
          final LinuxPackageManager manager = new LinuxPackageManager();
          manager.getPackage();
          manager.extractContents();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      } else {
        Logger.info("User is not using Linux. Proceeding to download Zip off Github.");
        try {
          final File zip = new File("VLC.zip");
          FileUtils.copyURLToFile(new URL(option), zip);
          final String path = zip.getAbsolutePath();
          final String dest = zip.getParent() + "/libs/vlc";
          Logger.info("Zip File Path: " + path);
          Logger.info("Extracting File...");
          ZipFileUtilities.decompressArchive(new File(path), new File(dest));
          Logger.info("Successfully Extracted File");
          Logger.info("Deleting Archive...");
          if (zip.delete()) {
            Logger.info("Archive deleted after installation.");
          } else {
            Logger.error("Archive could NOT be deleted after installation!");
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      Logger.info("Found VLC Installation! No need to install VLC beforehand.");
    }
  }
}
