/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary;

import com.github.pulsebeat02.minecraftmedialibrary.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.NMSReflectionManager;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.TinyProtocol;
import com.github.pulsebeat02.minecraftmedialibrary.utility.JavaVersionUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is the starting class of MinecraftMediaLibrary which describes the starting class for all
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
  private final Path parentFolder;
  private final Path httpParentFolder;
  private final Path dependenciesFolder;
  private final Path vlcFolder;
  private boolean vlcj;
  private boolean disabled;

  /**
   * Instantiates a new MinecraftMediaLibrary.
   *
   * @param plugin the plugin
   * @param isUsingVLCJ whether using vlcj
   */
  public MinecraftMediaLibrary(@NotNull final Plugin plugin, final boolean isUsingVLCJ) {
    this(plugin, null, null, null, isUsingVLCJ);
  }

  /**
   * Instantiates a new MinecraftMediaLibrary.
   *
   * @param plugin the plugin
   */
  public MinecraftMediaLibrary(@NotNull final Plugin plugin) {
    this(plugin, null, null, null, true);
  }

  /**
   * Instantiates a new MinecraftMediaLibrary
   *
   * @param plugin the plugin
   * @param http the path
   * @param libraryPath dependency path
   * @param vlcPath vlc installation path
   * @param isUsingVLCJ whether using vlcj
   */
  public MinecraftMediaLibrary(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      final boolean isUsingVLCJ) {
    final java.util.logging.Logger logger = plugin.getLogger();
    this.plugin = plugin;
    Logger.initializeLogger(this);
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
    final String path = String.format("%s/mml", plugin.getDataFolder().getAbsolutePath());
    parentFolder = Paths.get(path);
    httpParentFolder = Paths.get(http == null ? String.format("%s/http/", path) : http);
    dependenciesFolder =
        Paths.get(libraryPath == null ? String.format("%s/mml_libs/", path) : libraryPath);
    vlcFolder = Paths.get(vlcPath == null ? String.format("%s/vlc/", path) : vlcPath);
    createNecessaryFolders();
    vlcj = isUsingVLCJ;
    handler = NMSReflectionManager.getNewPacketHandlerInstance(this);
    listener = new PlayerJoinLeaveHandler(this);
    registerEvents();
    debugInformation();
    printSystemInformation();
    logger.info("Starting Dependency Tasks... this may take a while!");
    dependencyTasks();
    checkJavaVersion();
  }

  /** Creates the necessary folders required. */
  private void createNecessaryFolders() {
    final File parentHttpFile = httpParentFolder.toFile();
    final File dependenciesFile = dependenciesFolder.toFile();
    final File vlcjFile = vlcFolder.toFile();
    if (!parentHttpFile.isDirectory()) {
      if (parentHttpFile.mkdirs()) {
        Logger.info(
            String.format("Successfully created directory: %s", parentHttpFile.getAbsolutePath()));
      }
    }
    if (!dependenciesFile.isDirectory()) {
      if (dependenciesFile.mkdirs()) {
        Logger.info(
            String.format(
                "Successfully created directory: %s", dependenciesFile.getAbsolutePath()));
      }
    }
    if (!vlcjFile.isDirectory()) {
      if (vlcjFile.mkdirs()) {
        Logger.info(
            String.format("Successfully created directory: %s", vlcjFile.getAbsolutePath()));
      }
    }
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
    Logger.info(String.format("Plugin %s initialized MinecraftMediaLibrary", plugin.getName()));
    Logger.info("==================================================================");
    Logger.info(String.format("Path: %s", httpParentFolder));
    Logger.info(String.format("Using VLCJ? %s", vlcj ? "Yes" : "No"));
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
        String.format(
            "Windows/Mac/Linux: %s/%s/%s",
            RuntimeUtilities.isWindows(), RuntimeUtilities.isMac(), RuntimeUtilities.isLinux()));
    Logger.info("Linux Distribution (If Linux): " + RuntimeUtilities.getLinuxDistribution());
  }

  /** Shutdown Instance */
  public void shutdown() {
    Logger.info("Shutting Down!");
    disabled = true;
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
  public Path getPath() {
    return httpParentFolder;
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
  public Path getHttpParentFolder() {
    return httpParentFolder;
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
  public Path getDependenciesFolder() {
    return dependenciesFolder;
  }

  /**
   * Gets vlc.
   *
   * @return the vlc
   */
  public Path getVlcFolder() {
    return vlcFolder;
  }

  /**
   * Gets whether the library is disabled or not.
   *
   * @return the state
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  public Path getParentFolder() {
    return parentFolder;
  }
}
