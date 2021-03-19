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

package com.github.pulsebeat02.minecraftmedialibrary;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher;
import com.github.pulsebeat02.minecraftmedialibrary.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.NMSReflectionManager;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.TinyProtocol;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.io.File;
import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;

public final class MinecraftMediaLibrary {

  private final Plugin plugin;
  private final PlayerJoinLeaveHandler listener;
  private final PacketHandler handler;
  private final TinyProtocol protocol;
  /** Folder Paths to Use */
  private final String parent;

  private final String dependenciesFolder;
  private final String vlcFolder;
  private boolean vlcj;

  /**
   * Instantiates a new Minecraft media library.
   *
   * @param plugin the plugin
   * @param path the path
   * @param isUsingVLCJ the is using vlcj
   */
  public MinecraftMediaLibrary(
      @NotNull final Plugin plugin, @NotNull final String path, final boolean isUsingVLCJ) {
    this.plugin = plugin;
    protocol =
        new TinyProtocol(plugin) {
          @Override
          public Object onPacketOutAsync(
              final Player player, final Channel channel, final Object packet) {
            return handler.onPacketInterceptOut(player, packet);
          }

          @Override
          public Object onPacketInAsync(
              final Player player, final Channel channel, final Object packet) {
            return handler.onPacketInterceptIn(player, packet);
          }
        };
    handler = NMSReflectionManager.getNewPacketHandlerInstance(this);
    parent = path;
    final String prop = System.getProperty("user.dir");
    dependenciesFolder = prop + File.separator + "mml_libs";
    vlcFolder = prop + File.separator + "vlc";
    vlcj = isUsingVLCJ;
    listener = new PlayerJoinLeaveHandler(this);
    printSystemInformation();
    dependencyTasks();
    registrationTasks();
    debugInformation();
    checkJavaVersion();
  }

  public MinecraftMediaLibrary(
      @NotNull final Plugin plugin,
      @NotNull final String path,
      @NotNull final String libraryPath,
      @NotNull final String vlcPath,
      final boolean isUsingVLCJ) {
    this.plugin = plugin;
    protocol =
        new TinyProtocol(plugin) {
          @Override
          public Object onPacketOutAsync(
              final Player player, final Channel channel, final Object packet) {
            return handler.onPacketInterceptOut(player, packet);
          }

          @Override
          public Object onPacketInAsync(
              final Player player, final Channel channel, final Object packet) {
            return handler.onPacketInterceptIn(player, packet);
          }
        };
    handler = NMSReflectionManager.getNewPacketHandlerInstance(this);
    parent = path;
    dependenciesFolder = libraryPath;
    vlcFolder = vlcPath;
    vlcj = isUsingVLCJ;
    listener = new PlayerJoinLeaveHandler(this);
    printSystemInformation();
    dependencyTasks();
    registrationTasks();
    debugInformation();
    checkJavaVersion();
  }

  /** Runs dependency tasks required. */
  private void dependencyTasks() {

    class DependencyLoader {

      private final MinecraftMediaLibrary instance;

      public DependencyLoader() {
        instance = MinecraftMediaLibrary.this;
      }

      public void startTasks() {
        CompletableFuture.runAsync(this::assignClassLoader)
            .thenRunAsync(this::loadJave)
            .thenRunAsync(this::loadDependencies)
            .thenRunAsync(this::loadVLC);
      }

      /** Assigns ClassLoader for classpath loading. */
      public void assignClassLoader() {
        DependencyUtilities.CLASSLOADER = (URLClassLoader) plugin.getClass().getClassLoader();
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
        if (vlcj) {
          try {
            new MediaPlayerFactory();
          } catch (final Exception e) {
            Logger.error("The user does not have VLCJ installed! This is a very fatal error.");
            vlcj = false;
            e.printStackTrace();
          }
        }
      }
    }

    final DependencyLoader loader = new DependencyLoader();
    loader.startTasks();
  }

  /** Runs event registration tasks. */
  private void registrationTasks() {
    Bukkit.getPluginManager().registerEvents(listener, plugin);
  }

  /** Runs debug information. */
  private void debugInformation() {
    Logger.info("Plugin " + plugin.getName() + " initialized MinecraftMediaLibrary");
    Logger.info("==================================================================");
    Logger.info("Path: " + parent);
    Logger.info("Using VLCJ? " + (vlcj ? "Yes" : "No"));
    Logger.info("==================================================================");
  }

  /** Prompts warning based on Java Version */
  private void checkJavaVersion() {
    final String[] version = System.getProperty("java.version").split("\\.");
    final int major = Integer.parseInt(version[1]);
    if (major < 11) {
      Logger.warn(
          "MinecraftMediaPlugin is moving towards a newer Java Version (Java 11) \n"
              + "Please switch as soon as possible before the library will be incompatible \n"
              + "with your server. If you want to read more information surrounding this, \n"
              + "you may want to take a look here at "
              + "https://papermc.io/forums/t/java-11-mc-1-17-and-paper/5615");
    }
  }

  /** Print system information. */
  public void printSystemInformation() {
    Logger.info("===========================================");
    Logger.info("            SYSTEM INFORMATION             ");
    Logger.info("===========================================");
    Logger.info("System Operating System: " + RuntimeUtilities.getOperatingSystem());
    Logger.info("CPU Architecture: " + RuntimeUtilities.getCpuArch());
    Logger.info("System Operating System Version: " + System.getProperty("os.version"));
    Logger.info(
        "Windows/Mac/Linux: "
            + RuntimeUtilities.isWINDOWS()
            + "/"
            + RuntimeUtilities.isMAC()
            + "/"
            + RuntimeUtilities.isLINUX());
    Logger.info("Linux Distribution (If Linux): " + RuntimeUtilities.getLinuxDistribution());
  }

  /** Shutdown Instance */
  public void shutdown() {
    Logger.info("Shutting Down!");
    HandlerList.unregisterAll(listener);
    Logger.info("Good Bye");
  }

  /**
   * Gets plugin.
   *
   * @return the plugin
   */
  public Plugin getPlugin() {
    return plugin;
  }

  /**
   * Gets handler.
   *
   * @return the handler
   */
  public PacketHandler getHandler() {
    return handler;
  }

  /**
   * Gets protocol.
   *
   * @return the protocol
   */
  public TinyProtocol getProtocol() {
    return protocol;
  }

  /**
   * Gets path.
   *
   * @return the path
   */
  public String getPath() {
    return parent;
  }

  /**
   * Is using vlcj boolean.
   *
   * @return the boolean
   */
  public boolean isUsingVLCJ() {
    return vlcj;
  }

  /**
   * Gets parent.
   *
   * @return the parent
   */
  public String getParent() {
    return parent;
  }

  /**
   * Is vlcj boolean.
   *
   * @return the boolean
   */
  public boolean isVlcj() {
    return vlcj;
  }

  /**
   * Gets listener.
   *
   * @return the listener
   */
  public PlayerJoinLeaveHandler getListener() {
    return listener;
  }

  /**
   * Gets dependencies.
   *
   * @return the dependencies
   */
  public String getDependenciesFolder() {
    return dependenciesFolder;
  }

  /**
   * Gets vlc.
   *
   * @return the vlc
   */
  public String getVlcFolder() {
    return vlcFolder;
  }
}
