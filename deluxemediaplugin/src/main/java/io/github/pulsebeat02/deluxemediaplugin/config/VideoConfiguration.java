/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.ServerPropertyMutator;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.VLCPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VideoConfiguration extends ConfigurationProvider {

  private io.github.pulsebeat02.minecraftmedialibrary.frame.VLCPlayer player;
  private MapDataCallback callback;

  public VideoConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/video.yml");
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
    final boolean forceThreshold = configuration.getBoolean("force-packet-threshold");
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
      final MediaLibrary library = getPlugin().library();
      if (forceThreshold) {
        new ServerPropertyMutator().mutateCompressionThreshold();
      }
      final MapDataCallback callback =
          MapDataCallback.builder()
              .viewers(null)
              .ditherHolder(holder)
              .map(startingMapID)
              .itemframeWidth(frameWidth)
              .itemframeHeight(frameHeight)
              .videoWidth(width)
              .delay(0)
              .build(library);
      if (vlcj) {
        if (url == null) {
          Logger.info("URL in video.yml is not a valid or specified url!");
        } else {
          player =
              VLCPlayer.builder()
                  .url(url)
                  .callback(callback)
                  .width(width)
                  .height(height)
                  .build(library);
        }
      }
      this.callback = callback;
    }
  }

  public io.github.pulsebeat02.minecraftmedialibrary.frame.VLCPlayer getPlayer() {
    return player;
  }

  public MapDataCallbackPrototype getCallback() {
    return callback;
  }
}
