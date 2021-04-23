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

    // Deserialize the audio encoder settings
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("bitrate", settings.getBitrate());
    configuration.set("channels", settings.getChannels());
    configuration.set("sampling-rate", settings.getSamplingRate());
    configuration.set("volume", settings.getVolume());
    saveConfig();
  }

  @Override
  void serialize() {

    // Read the encoder settings
    final FileConfiguration configuration = getFileConfiguration();

    /*

    Get the bitrate at which the audio should have
    (Ex: 160000 for decent quality)

    */
    final int bitrate = configuration.getInt("bitrate");

    /*

    Get the number of channels the audio should have
    (Ex: 1 for mono-audio, 2 for natural hearing or audio from both sides of the speaker)

    */
    final int channels = configuration.getInt("channels");

    /*

    Get the sampling rate the audio should have
    (Ex: 44100 samples per second)

    */
    final int samplingRate = configuration.getInt("sampling-rate");

    /*

    Get the volume the audio should be
    (Ex: 48 for normal)

    */
    final int volume = configuration.getInt("volume");

    // Create a new audio extraction configuration to be used
    settings = new ExtractionSetting(bitrate, channels, samplingRate, volume);
  }

  public ExtractionSetting getSettings() {
    return settings;
  }
}
