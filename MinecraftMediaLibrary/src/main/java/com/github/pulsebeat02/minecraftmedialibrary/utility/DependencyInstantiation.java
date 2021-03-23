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

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;

public class DependencyInstantiation {

  private final MinecraftMediaLibrary instance;

  public DependencyInstantiation(@NotNull final MinecraftMediaLibrary library) {
    instance = library;
  }

  public void startTasks() {
    CompletableFuture.runAsync(this::assignClassLoader)
        .thenRunAsync(this::loadJave)
        .thenRunAsync(this::loadDependencies)
        .thenRunAsync(this::loadVLC);
  }

  /** Assigns ClassLoader for classpath loading. */
  public void assignClassLoader() {
    DependencyUtilities.CLASSLOADER =
        (URLClassLoader) instance.getPlugin().getClass().getClassLoader();
  }

  /** Downloads/Loads Jave dependency. */
  public void loadJave() {
    final JaveDependencyInstallation javeDependencyInstallation =
        new JaveDependencyInstallation(instance);
    javeDependencyInstallation.install();
    javeDependencyInstallation.load();
  }

  /** Downloads/Loads Jitpack/Maven dependencies. */
  public void loadDependencies() {
    final DependencyManagement dependencyManagement = new DependencyManagement(instance);
    dependencyManagement.install();
    dependencyManagement.relocate();
    dependencyManagement.load();
  }

  /** Downloads/Loads VLC dependency. */
  public void loadVLC() {
    new VLCNativeDependencyFetcher(instance).downloadLibraries();
    if (instance.isUsingVLCJ()) {
      try {
        new MediaPlayerFactory();
      } catch (final Exception e) {
        Logger.error("The user does not have VLCJ installed! This is a very fatal error.");
        instance.setVlcj(false);
        e.printStackTrace();
      }
    }
  }
}
