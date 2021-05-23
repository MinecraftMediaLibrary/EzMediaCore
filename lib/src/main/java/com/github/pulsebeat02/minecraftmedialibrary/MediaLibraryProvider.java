package com.github.pulsebeat02.minecraftmedialibrary;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MediaLibraryProvider {

  private MediaLibraryProvider() {}

  public static MediaLibrary create(final Plugin plugin) {
    return new MinecraftMediaLibrary(plugin);
  }

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
