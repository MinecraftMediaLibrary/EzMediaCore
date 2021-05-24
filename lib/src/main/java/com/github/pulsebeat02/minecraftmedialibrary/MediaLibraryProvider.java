package com.github.pulsebeat02.minecraftmedialibrary;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MediaLibraryProvider {

  private MediaLibraryProvider() {}

  /**
   * Static builder to create a media library from plugin.
   *
   * @param plugin the plugin
   * @return a new MediaLibrary instance
   */
  public static MediaLibrary create(final Plugin plugin) {
    return new MinecraftMediaLibrary(plugin);
  }

  /**
   * Static builder to create a media library from plugin.
   *
   * @param plugin the plugin
   * @param http the http
   * @param libraryPath the library path
   * @param vlcPath the vlc path
   * @param imagePath the image path
   * @param audioPath the audio path
   * @param isUsingVLCJ if user is using VLCJ
   * @return a new MediaLibrary instance
   */
  public static MediaLibrary create(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      @Nullable final String imagePath,
      @Nullable final String audioPath,
      final boolean isUsingVLCJ) {
    return new MinecraftMediaLibrary(
        plugin, http, libraryPath, vlcPath, imagePath, audioPath, isUsingVLCJ);
  }
}
