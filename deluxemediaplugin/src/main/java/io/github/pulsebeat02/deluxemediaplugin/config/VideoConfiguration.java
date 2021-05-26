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
import io.github.pulsebeat02.minecraftmedialibrary.frame.VLCVideoPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherHolder;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallbackPrototype;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapIntegratedPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VideoConfiguration extends AbstractConfiguration {

  private VLCVideoPlayer player;
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
      final MediaLibrary library = player.getLibrary();

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

  public VLCVideoPlayer getPlayer() {
    return player;
  }

  public MapDataCallbackPrototype getCallback() {
    return callback;
  }
}
