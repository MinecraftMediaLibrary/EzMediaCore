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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public abstract class AbstractConfiguration {

  private final DeluxeMediaPlugin plugin;
  private final String fileName;

  private final File configFile;
  private FileConfiguration fileConfiguration;

  public AbstractConfiguration(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String name) {
    this.plugin = plugin;
      fileName = name;
      configFile = new File(plugin.getDataFolder(), fileName);
  }

  public void reloadConfig() {
    fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
    final InputStream defConfigStream = plugin.getResource(fileName);
    if (defConfigStream != null) {
      final YamlConfiguration defConfig =
          YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
      fileConfiguration.setDefaults(defConfig);
    }
  }

  public FileConfiguration getConfig() {
    if (fileConfiguration == null) {
        reloadConfig();
    }
    return fileConfiguration;
  }

  public void saveConfig() {
    if (fileConfiguration != null && configFile != null) {
      try {
        getConfig().save(configFile);
      } catch (final IOException e) {
        plugin
            .getLogger()
            .log(Level.SEVERE, String.format("Could not save config to %s", configFile), e);
      }
    }
  }

  public void saveDefaultConfig() {
    if (!configFile.exists()) {
      plugin.saveResource(fileName, false);
    }
  }

  public void read() {
    if (!configFile.exists()) {
      saveDefaultConfig();
    }
    getConfig();
    serialize();
  }

  abstract void deserialize();

  abstract void serialize();

  public DeluxeMediaPlugin getPlugin() {
    return plugin;
  }

  public String getFileName() {
    return fileName;
  }

  public File getConfigFile() {
    return configFile;
  }

  public FileConfiguration getFileConfiguration() {
    return fileConfiguration;
  }
}
