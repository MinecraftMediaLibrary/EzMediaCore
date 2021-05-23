package com.github.pulsebeat02.minecraftmedialibrary;

import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

/** The base media library class used the media library. */
public interface MediaLibrary {

  /** Shutdown the library instance */
  void shutdown();

  /**
   * Gets plugin.
   *
   * @return the plugin
   */
  Plugin getPlugin();

  /**
   * Gets handler.
   *
   * @return the handler
   */
  PacketHandler getHandler();

  /**
   * Whether the library is using vlcj.
   *
   * @return the boolean
   */
  boolean isVlcj();

  /**
   * Sets the usage status of vlcj.
   *
   * @param vlcj status
   */
  void setVlcj(boolean vlcj);

  /**
   * Gets the path of the parent library folder.
   *
   * @return the path
   */
  Path getPath();

  /**
   * Gets the http parent folder.
   *
   * @return the parent
   */
  Path getHttpParentFolder();

  /**
   * Gets dependencies folder.
   *
   * @return the dependencies folder
   */
  Path getDependenciesFolder();

  /**
   * Gets the vlc folder.
   *
   * @return the vlc folder
   */
  Path getVlcFolder();

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  Path getParentFolder();

  /**
   * Gets the image folder of the library.
   *
   * @return the path of the image folder
   */
  Path getImageFolder();

  /**
   * Gets the audio folder of the library.
   *
   * @return the path of the audio folder
   */
  Path getAudioFolder();

  /**
   * Gets the library path handle.
   *
   * @return the path handle
   */
  LibraryPathHandle getHandle();

  /**
   * Returns the status of the library.
   *
   * @return whether the library is disabled or not
   */
  boolean isDisabled();

  /**
   * Gets the listener.
   *
   * @return the listener
   */
  Listener getRegistrationHandler();
}
