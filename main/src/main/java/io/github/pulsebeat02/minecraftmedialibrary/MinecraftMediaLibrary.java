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

package io.github.pulsebeat02.minecraftmedialibrary;

import io.github.pulsebeat02.minecraftmedialibrary.listener.PlayerJoinLeaveRegistration;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import io.github.pulsebeat02.minecraftmedialibrary.reflection.NMSReflectionManager;
import io.github.pulsebeat02.minecraftmedialibrary.reflection.TinyProtocol;
import io.github.pulsebeat02.minecraftmedialibrary.utility.DebuggerUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.JavaVersionUtilities;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * This is the starting class of MinecraftMediaLibrary which describes the starting class for all
 * modules. It passes in a Plugin as an argument with a path to the http daemon. Optional arguments
 * include passing in a dependency path and a vlc dependency path. The boolean for vlcj is used to
 * determine whether the plugin should support vlc or not. MinecraftMediaLibrary will hook into the
 * Plugin, make a listener to register players, and many other extra necessities that are registered
 * under the specific plugin.
 */
public final class MinecraftMediaLibrary implements MediaLibrary {

  private final Plugin plugin;
  private final PacketHandler handler;
  private final PlayerJoinLeaveRegistration registrationHandler;

  private final LibraryPathHandle handle;
  private boolean vlcj;
  private boolean disabled;

  /**
   * Instantiates a new MinecraftMediaLibrary.
   *
   * @param plugin the plugin
   */
  MinecraftMediaLibrary(@NotNull final Plugin plugin) {
    this(plugin, null, null, null, null, null, true);
  }

  /**
   * Instantiates a new MinecraftMediaLibrary
   *
   * @param plugin the plugin
   * @param http the path
   * @param libraryPath dependency path
   * @param vlcPath vlc installation path
   * @param audioPath audio path
   * @param imagePath image path
   * @param isUsingVLCJ whether using vlcj
   */
  MinecraftMediaLibrary(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      @Nullable final String imagePath,
      @Nullable final String audioPath,
      final boolean isUsingVLCJ) {

    this.plugin = plugin;

    Logger.initializeLogger(this);
    Logger.setVerbose(true);

    JavaVersionUtilities.sendWarningMessage();
    DebuggerUtilities.getDebugInformation(this);

    registrationHandler = new PlayerJoinLeaveRegistration(this);
    Bukkit.getPluginManager().registerEvents(registrationHandler, plugin);

    handle = new LibraryPathHandle(plugin, http, libraryPath, vlcPath, imagePath, audioPath);

    handler = NMSReflectionManager.getNewPacketHandlerInstance();
    if (handler != null) {
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

      vlcj = isUsingVLCJ;

      dependencyTasks();
      Logger.info("Finished Initializing Library!");

    } else {

      shutdown();
    }
  }

  /** Runs dependency tasks required. */
  private void dependencyTasks() {
    Logger.info(
        "Starting Dependency Tasks... This may take a while depending on your environment!");
    new DependencyInstantiation(this).startTasks();
  }

  /** Shutdown the library instance */
  @Override
  public void shutdown() {
    Logger.info("Shutting Down!");
    disabled = true;
    HandlerList.unregisterAll(registrationHandler);
    Logger.info("Good Bye");
  }

  /**
   * Gets plugin.
   *
   * @return the plugin
   */
  @Override
  public Plugin getPlugin() {
    return plugin;
  }

  /**
   * Gets handler.
   *
   * @return the handler
   */
  @Override
  public PacketHandler getHandler() {
    return handler;
  }

  /**
   * Whether the library is using vlcj.
   *
   * @return the boolean
   */
  @Override
  public boolean isVlcj() {
    return vlcj;
  }

  /**
   * Sets the usage status of vlcj.
   *
   * @param vlcj status
   */
  @Override
  public void setVlcj(final boolean vlcj) {
    this.vlcj = vlcj;
  }

  /**
   * Gets the listener.
   *
   * @return the listener
   */
  @Override
  public Listener getRegistrationHandler() {
    return registrationHandler;
  }

  /**
   * Gets the path of the parent library folder.
   *
   * @return the path
   */
  @Override
  public Path getPath() {
    return handle.getParentFolder();
  }

  /**
   * Gets the http parent folder.
   *
   * @return the parent
   */
  @Override
  public Path getHttpParentFolder() {
    return handle.getHttpParentFolder();
  }

  /**
   * Gets dependencies folder.
   *
   * @return the dependencies folder
   */
  @Override
  public Path getDependenciesFolder() {
    return handle.getDependenciesFolder();
  }

  /**
   * Gets the vlc folder.
   *
   * @return the vlc folder
   */
  @Override
  public Path getVlcFolder() {
    return handle.getVlcFolder();
  }

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  @Override
  public Path getParentFolder() {
    return handle.getParentFolder();
  }

  /**
   * Gets the image folder of the library.
   *
   * @return the path of the image folder
   */
  @Override
  public Path getImageFolder() {
    return handle.getImageFolder();
  }

  /**
   * Gets the audio folder of the library.
   *
   * @return the path of the audio folder
   */
  @Override
  public Path getAudioFolder() {
    return handle.getAudioFolder();
  }

  /**
   * Gets the library path handle.
   *
   * @return the path handle
   */
  @Override
  public LibraryPathHandle getHandle() {
    return handle;
  }

  /**
   * Returns the status of the library.
   *
   * @return whether the library is disabled or not
   */
  @Override
  public boolean isDisabled() {
    return disabled;
  }
}
