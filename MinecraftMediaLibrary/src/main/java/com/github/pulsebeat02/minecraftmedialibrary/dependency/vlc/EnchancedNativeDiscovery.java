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
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class EnchancedNativeDiscovery implements NativeDiscoveryStrategy {

  private final String dir;
  private String path;

  /** Instantiates a new EnchancedNativeDiscovery */
  public EnchancedNativeDiscovery(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
  }

  /** Instantiates a new EnchancedNativeDiscovery */
  public EnchancedNativeDiscovery(@NotNull final String dir) {
    this.dir = dir;
  }

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
    final File fold = new File(dir);
    if (!fold.exists()) {
      return null;
    }
    final Queue<File> folders = new ArrayDeque<>(Arrays.asList(fold.listFiles()));
    while (!folders.isEmpty()) {
      final File f = folders.remove();
      if (f.isDirectory()) {
        if (f.getName().equals("plugins")) {
          path = f.getAbsolutePath();
          onSetPluginPath(path);
          return path;
        }
        folders.addAll(Arrays.asList(f.listFiles()));
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
    return LibC.INSTANCE.setenv("VLC_PLUGIN_PATH", path, 1) == 0;
  }

  /**
   * Gets the directory.
   *
   * @return dir directory
   */
  public String getDir() {
    return dir;
  }

  /**
   * Gets the path.
   *
   * @return path path
   */
  public String getPath() {
    return path;
  }
}
