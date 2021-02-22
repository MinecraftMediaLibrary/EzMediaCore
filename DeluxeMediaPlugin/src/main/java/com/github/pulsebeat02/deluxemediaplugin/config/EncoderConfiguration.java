/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.ExtractionSetting;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class EncoderConfiguration extends AbstractConfiguration {

  private ExtractionSetting settings;

  public EncoderConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "encoder.yml");
  }

  @Override
  void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("bitrate", settings.getBitrate());
    configuration.set("channels", settings.getChannels());
    configuration.set("sampling-rate", settings.getSamplingRate());
    configuration.set("volume", settings.getVolume());
    saveConfig();
  }

  @Override
  void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    int bitrate = configuration.getInt("bitrate");
    int channels = configuration.getInt("channels");
    int samplingRate = configuration.getInt("sampling-rate");
    int volume = configuration.getInt("volume");
    settings = new ExtractionSetting(bitrate, channels, samplingRate, volume);
  }

  public ExtractionSetting getSettings() {
    return settings;
  }
}
