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
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VLCJIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VideoPlayerBase;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VideoConfiguration extends AbstractConfiguration {

  private VideoPlayerBase player;
  private ItemFrameCallback callback;

  public VideoConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "video.yml");
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    configuration.set("enabled", player != null);
    configuration.set("url", player.getUrl());
    configuration.set("video-width", player.getWidth());
    configuration.set("video-height", player.getHeight());
    configuration.set("itemframe-width", callback.getWidth());
    configuration.set("itemframe-height", callback.getHeight());
    configuration.set("starting-map-id", callback.getMap());
    configuration.set("dither-setting", callback.getType().getSetting().name());
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    final boolean enabled = configuration.getBoolean("enabled");
    final String url = configuration.getString("url");
    final int width = configuration.getInt("video-width");
    final int height = configuration.getInt("video-height");
    final int frameWidth = configuration.getInt("itemframe-width");
    final int frameHeight = configuration.getInt("itemframe-height");
    final int startingMapID = configuration.getInt("starting-map-id");
    final String ditherSetting = configuration.getString("dither-setting");
    DitherHolder holder = null;
    for (final DitherSetting setting : DitherSetting.values()) {
      if (setting.name().equalsIgnoreCase(ditherSetting)) {
        holder = setting.getHolder();
        break;
      }
    }
    if (holder == null) {
      Logger.error(
          String.format(
              "Setting %s in video.yml is NOT a valid dithering algorithm! Resorting to Filter Lite Algorithm!",
              ditherSetting));
      holder = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    }
    final boolean vlcj = configuration.getBoolean("using-vlcj");
    if (enabled) {
      final MinecraftMediaLibrary library = player.getLibrary();
      final ItemFrameCallback callback =
          new ItemFrameCallback(
              getPlugin().getLibrary(),
              null,
              startingMapID,
              frameWidth,
              frameHeight,
              width,
              0,
              holder);
      if (vlcj) {
        if (url == null) {
          Logger.info("URL in video.yml is not a valid or specified url!");
        } else {
          player = new VLCJIntegratedPlayer(library, url, width, height, callback::send);
        }
      }
      this.callback = callback;
    }
  }

  public VideoPlayerBase getPlayer() {
    return player;
  }

  public ItemFrameCallback getCallback() {
    return callback;
  }
}
