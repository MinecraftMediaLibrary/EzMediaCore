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

import java.net.URLClassLoader;

public final class MinecraftMediaLibrary {

  private final Plugin plugin;
  private final String parent;
  private final PlayerJoinLeaveHandler listener;
  private final PacketHandler handler;
  private final TinyProtocol protocol;
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
                          @NotNull final Player player,
                          @NotNull final Channel channel,
                          @NotNull final Object packet) {
                      return handler.onPacketInterceptOut(player, packet);
                  }

                  @Override
                  public Object onPacketInAsync(
                          @NotNull final Player player,
                          @NotNull final Channel channel,
                          @NotNull final Object packet) {
                      return handler.onPacketInterceptIn(player, packet);
                  }
              };
      handler = NMSReflectionManager.getNewPacketHandlerInstance(this);
    parent = path;
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
    DependencyUtilities.CLASSLOADER = (URLClassLoader) plugin.getClass().getClassLoader();
    final JaveDependencyInstallation javeDependencyInstallation = new JaveDependencyInstallation();
    javeDependencyInstallation.install();
    javeDependencyInstallation.load();
    final DependencyManagement dependencyManagement = new DependencyManagement();
    dependencyManagement.install();
    dependencyManagement.relocate();
    dependencyManagement.load();
    new VLCNativeDependencyFetcher().downloadLibraries();
    if (vlcj) {
      try {
        new MediaPlayerFactory();
      } catch (final Exception e) {
          Logger.error(
                  "The user does not have VLCJ installed! This is a very fatal error.");
          vlcj = false;
      }
    }
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

  public void printSystemInformation() {
    Logger.info("===========================================");
    Logger.info("            SYSTEM INFORMATION             ");
    Logger.info("===========================================");
    Logger.info("System Operating System: " + RuntimeUtilities.OPERATING_SYSTEM);
    Logger.info("CPU Architecture: " + RuntimeUtilities.CPU_ARCH);
    Logger.info("System Operating System Version: " + System.getProperty("os.version"));
    Logger.info("Windows/Mac/Linux: " + RuntimeUtilities.WINDOWS
            + "/" + RuntimeUtilities.MAC
            + "/" + RuntimeUtilities.LINUX);
    Logger.info("Linux Distribution (If Linux): " + RuntimeUtilities.LINUX_DISTRIBUTION);
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
}
