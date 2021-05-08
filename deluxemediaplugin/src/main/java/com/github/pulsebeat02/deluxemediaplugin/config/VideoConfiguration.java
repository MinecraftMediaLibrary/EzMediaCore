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
import com.github.pulsebeat02.minecraftmedialibrary.frame.VideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VideoConfiguration extends AbstractConfiguration {

  private VideoPlayer player;
  private MapDataCallback callback;

  public VideoConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "video.yml");
  }

  @Override
  public void deserialize() {

    // Store the video settings into a config file
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

    // Get the video settings and read it into an actual VideoPlayer
    final FileConfiguration configuration = getFileConfiguration();

    // Get whether vlc is enabled or not
    final boolean enabled = configuration.getBoolean("enabled");

    // Get the mrl of the media file
    final String url = configuration.getString("url");

    // Get the video player width in pixels
    final int width = configuration.getInt("video-width");

    // Get the video player height in pixels
    final int height = configuration.getInt("video-height");

    // Get the itemframe's width (default set to 5)
    final int frameWidth = configuration.getInt("itemframe-width");

    // Get the itemframe's height (default set to 5)
    final int frameHeight = configuration.getInt("itemframe-height");

    // Get the starting map id
    final int startingMapID = configuration.getInt("starting-map-id");

    // Get the correct dithering option
    final String ditherSetting = configuration.getString("dither-setting");

    // Find if the Dithering option is one that is valid
    DitherHolder holder = null;
    for (final DitherSetting setting : DitherSetting.values()) {
      if (setting.name().equalsIgnoreCase(ditherSetting)) {
        holder = setting.getHolder();
        break;
      }
    }

    // If it isn't valid, resort to Filter Lite Dithering
    if (holder == null) {
      Logger.error(
          String.format(
              "Setting %s in video.yml is NOT a valid dithering algorithm! Resorting to Filter Lite Algorithm!",
              ditherSetting));
      holder = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    }

    // Check if the plugin is using VLCJ (requirement)
    final boolean vlcj = configuration.getBoolean("using-vlcj");
    if (enabled) {

      // Get the library instance
      final MinecraftMediaLibrary library = player.getLibrary();

      /*

      Instantiate a new Map data callback (for displaying onto itemframes)
      Pass in arguments such as the viewers (stored in a UUID[]), the type of dithering
      method you want to use ("holder"), the starting map id (for example, 0 - 24, 1 - 24, etc), the
      frameWidth (itemframe width), frameHeight (itemframe height), video width (width of the video
      player), the delay, and the library instance

      */
      final MapDataCallback callback =
          MapDataCallback.builder()
              .setViewers(null)
              .setDitherHolder(holder)
              .setMap(startingMapID)
              .setItemframeWidth(frameWidth)
              .setItemframeHeight(frameHeight)
              .setVideoWidth(width)
              .setDelay(0)
              .build(library);

      if (vlcj) {
        if (url == null) {

          // If the url is not valid, it means the video media is incorrect
          Logger.info("URL in video.yml is not a valid or specified url!");

        } else {

          /*

          Instantiate a new MapIntegratedPlayer with the url media, the specific callback
          to use (defined above), the width of the video, the height of the video, and
          the library instance.

           */
          player =
              MapIntegratedPlayer.builder()
                  .setUrl(url)
                  .setCallback(callback)
                  .setWidth(width)
                  .setHeight(height)
                  .build(library);
        }
      }
      this.callback = callback;
    }
  }

  public VideoPlayer getPlayer() {
    return player;
  }

  public MapDataCallback getCallback() {
    return callback;
  }
}
