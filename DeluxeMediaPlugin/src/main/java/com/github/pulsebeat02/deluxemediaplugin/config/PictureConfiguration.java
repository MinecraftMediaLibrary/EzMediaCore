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
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.image.basic.MinecraftStaticImage;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PictureConfiguration extends AbstractConfiguration {

  private final Set<MinecraftStaticImage> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "picture.yml");
    images = new HashSet<>();
  }

  public void addPhoto(final int map, @NotNull final File file, final int width, final int height) {

    // Add an image
    images.add(
        MinecraftStaticImage.builder()
            .setMap(map)
            .setImage(file)
            .setWidth(width)
            .setHeight(height)
            .build(getPlugin().getLibrary()));
  }

  @Override
  public void deserialize() {

    // Deserialize the images settings
    final FileConfiguration configuration = getFileConfiguration();
    for (final MinecraftStaticImage image : images) {
      final long key = image.getMap();
      configuration.set(String.format("%d.location", key), image.getImage().getAbsolutePath());
      configuration.set(String.format("%d.width", key), image.getWidth());
      configuration.set(String.format("%d.height", key), image.getHeight());
    }
    saveConfig();
  }

  @Override
  public void serialize() {

    // Read the images from the configuration file
    final FileConfiguration configuration = getFileConfiguration();

    // Get library instance
    final MinecraftMediaLibrary library = getPlugin().getLibrary();

    for (final String key : configuration.getKeys(false)) {

      // Get the map id
      final int id = Integer.parseInt(key);

      // Get the file path of the image
      final File file =
          new File(
              Objects.requireNonNull(configuration.getString(String.format("%d.location", id))));

      // If it doesn't exist, throw an error
      if (!file.exists()) {
        Logger.error(String.format("Could not read %s at id %d!", file.getAbsolutePath(), id));
        continue;
      }

      // Get the width of the image
      final int width = configuration.getInt(String.format("%d.width", id));

      // Get the height of the image
      final int height = configuration.getInt(String.format("%d.height", id));

      // Define a new image with the specified id
      images.add(
          MinecraftStaticImage.builder()
              .setMap(id)
              .setImage(file)
              .setWidth(width)
              .setHeight(height)
              .build(library));
    }
  }

  public Set<MinecraftStaticImage> getImages() {
    return images;
  }
}
