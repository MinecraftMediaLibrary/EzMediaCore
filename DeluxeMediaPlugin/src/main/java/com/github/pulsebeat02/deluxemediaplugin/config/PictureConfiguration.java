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

package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.image.MinecraftMapImage;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PictureConfiguration extends AbstractConfiguration {

  private final Set<MinecraftMapImage> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "picture.yml");
    images = new HashSet<>();
  }

  public void addPhoto(final int map, @NotNull final File file, final int width, final int height) {
    images.add(new MinecraftMapImage(getPlugin().getLibrary(), map, file, width, height));
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    for (final MinecraftMapImage image : images) {
      final long key = image.getMap();
      configuration.set(String.format("%d.location", key), image.getImage().getAbsolutePath());
      configuration.set(String.format("%d.width", key), image.getWidth());
      configuration.set(String.format("%d.height", key), image.getHeight());
    }
    saveConfig();
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    for (final String key : configuration.getKeys(false)) {
      final int id = Integer.parseInt(key);
      final File file =
          new File(
              Objects.requireNonNull(configuration.getString(String.format("%d.location", id))));
      if (!file.exists()) {
        Logger.error(String.format("Could not read %s at id %d!", file.getAbsolutePath(), id));
        continue;
      }
      final int width = configuration.getInt(String.format("%dwidth", id));
      final int height = configuration.getInt(String.format("%dheight", id));
      images.add(new MinecraftMapImage(getPlugin().getLibrary(), id, file, width, height));
    }
  }

  public Set<MinecraftMapImage> getImages() {
    return images;
  }
}
