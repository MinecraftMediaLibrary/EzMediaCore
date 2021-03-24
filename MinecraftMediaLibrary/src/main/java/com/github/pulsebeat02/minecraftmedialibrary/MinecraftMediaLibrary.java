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

import com.github.pulsebeat02.minecraftmedialibrary.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.NMSReflectionManager;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.TinyProtocol;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyInstantiation;
import com.github.pulsebeat02.minecraftmedialibrary.utility.JavaVersionUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * <p> This is the starting class of MinecraftMediaLibrary which describes the starting class for all
 * modules. It passes in a Plugin as an argument with a path to the http daemon. Optional arguments
 * include passing in a dependency path and a vlc dependency path. The boolean for vlcj is used to
 * determine whether the plugin should support vlc or not. MinecraftMediaLibrary will hook into the
 * Plugin, make a listener to register players, and many other extra necessities that are registered
 * under the specific plugin.
 */
public final class MinecraftMediaLibrary {

  private final Plugin plugin;
  private final PlayerJoinLeaveHandler listener;
  private final PacketHandler handler;
  private final TinyProtocol protocol;
  private final String parent;
  private final String dependenciesFolder;
  private final String vlcFolder;
  private boolean vlcj;

  /**
   * Instantiates a new MinecraftMediaLibrary.
   *
   * @param plugin the plugin
   * @param path the path
   * @param isUsingVLCJ whether using vlcj
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
    registerEvents();
    debugInformation();
    checkJavaVersion();
  }

  /**
   * Instantiates a new MinecraftMediaLibrary
   *
   * @param plugin the plugin
   * @param path the path
   * @param libraryPath dependency path
   * @param vlcPath vlc installation path
   * @param isUsingVLCJ whether using vlcj
   */
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
    registerEvents();
    debugInformation();
    checkJavaVersion();
  }

  /** Runs dependency tasks required. */
  private void dependencyTasks() {
    new DependencyInstantiation(this).startTasks();
  }

  /** Runs event registration tasks. */
  private void registerEvents() {
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
    new JavaVersionUtilities().sendWarningMessage();
  }

  /** Print system information. */
  private void printSystemInformation() {
    Logger.info("===========================================");
    Logger.info("            SYSTEM INFORMATION             ");
    Logger.info("===========================================");
    Logger.info("System Operating System: " + RuntimeUtilities.getOperatingSystem());
    Logger.info("CPU Architecture: " + RuntimeUtilities.getCpuArch());
    Logger.info("System Operating System Version: " + System.getProperty("os.version"));
    Logger.info(
        "Windows/Mac/Linux: "
            + RuntimeUtilities.isWindows()
            + "/"
            + RuntimeUtilities.isMac()
            + "/"
            + RuntimeUtilities.isLinux());
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
   * Sets status of vlcj.
   *
   * @param vlcj status
   */
  public void setVlcj(final boolean vlcj) {
    this.vlcj = vlcj;
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
