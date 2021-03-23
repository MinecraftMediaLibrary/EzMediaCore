/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.sun.jna.NativeLibrary;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public abstract class SilentOSDependentSolution {

  private final String dir;
  private final NativeDiscovery nativeDiscovery;

  public SilentOSDependentSolution(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
    nativeDiscovery = new NativeDiscovery();
  }

  public SilentOSDependentSolution(@NotNull final String dir) {
    this.dir = dir;
    nativeDiscovery = new NativeDiscovery();
  }

  /** Download VLC libraries. */
  public abstract void downloadVLCLibrary() throws IOException;

  /**
   * Checks existence of VLC folder.
   *
   * @param dir directory
   */
  public boolean checkVLCExistance(@NotNull final String dir) {
    final File folder = findVLCFolder(new File(dir));
    if (folder == null || !folder.exists()) {
      return false;
    }
    loadNativeDependency(folder);
    return getNativeDiscovery().discover();
  }

  /**
   * Loads native dependency from file.
   *
   * @param folder directory
   */
  public void loadNativeDependency(@NotNull final File folder) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), folder.getAbsolutePath());
    nativeDiscovery.discover();
  }

  /** Prints all System environment variables. */
  public void printSystemEnvironmentVariables() {
    Logger.info("======== SYSTEM ENVIRONMENT VARIABLES ========");
    for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
      Logger.info("Key: " + entry.getKey() + "| Entry: " + entry.getValue());
    }
    Logger.info("==============================================");
  }

  /** Prints all System properties. */
  public void printSystemProperties() {
    Logger.info("============== SYSTEM PROPERTIES ==============");
    final Properties p = System.getProperties();
    final Enumeration<Object> keys = p.keys();
    while (keys.hasMoreElements()) {
      final String key = (String) keys.nextElement();
      Logger.info("Key: " + key + "| Entry: " + p.get(key));
    }
    Logger.info("===============================================");
  }

  /**
   * Gets VLC folder in folder.
   *
   * @param folder search folder
   * @return file
   */
  public File findVLCFolder(@NotNull final File folder) {
    for (final File f : folder.listFiles()) {
      final String name = f.getName();
      if (StringUtils.containsIgnoreCase(name, "vlc") && !name.endsWith(".dmg")) {
        return f;
      }
    }
    return null;
  }

  /**
   * Deletes file (archive).
   *
   * @param zip archive
   */
  public void deleteArchive(@NotNull final File zip) {
    Logger.info("Deleting Archive...");
    if (zip.delete()) {
      Logger.info("Archive deleted after installation.");
    } else {
      Logger.error("Archive could NOT be deleted after installation!");
    }
  }

  /**
   * Gets directory of file.
   *
   * @return directory
   */
  public String getDir() {
    return dir;
  }

  /**
   * Gets NativeDiscovery.
   *
   * @return native discovery
   */
  public NativeDiscovery getNativeDiscovery() {
    return nativeDiscovery;
  }
}
