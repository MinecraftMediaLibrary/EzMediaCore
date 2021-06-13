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

package io.github.pulsebeat02.minecraftmedialibrary.image.gif;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import io.github.pulsebeat02.minecraftmedialibrary.frame.map.VLCPlayer;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import org.bukkit.Bukkit;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DynamicImage implements DynamicImageProxy {

  private final MediaLibrary library;
  private final Path image;
  private final int map;
  private final int height;
  private final int width;

  /**
   * Instantiates a new DynamicImage.
   *
   * @param library the library
   * @param map the map
   * @param image the image
   * @param width the width
   * @param height the height
   */
  public DynamicImage(
      @NotNull final MediaLibrary library,
      final int map,
      @NotNull final Path image,
      final int width,
      final int height) {
    Preconditions.checkArgument(Files.exists(image), "Image does not exist!");
    this.library = library;
    this.map = map;
    this.image = image;
    this.width = width;
    this.height = height;
    Logger.info(
        String.format("Initialized Image at Map ID %d (Source: %s)", map, image.toAbsolutePath()));
  }

  /**
   * Instantiates a new MapImage.
   *
   * @param library the library
   * @param map the map
   * @param url the url
   * @param width the width
   * @param height the height
   */
  public DynamicImage(
      @NotNull final MediaLibrary library,
      final int map,
      @NotNull final String url,
      final int width,
      final int height) {
    this.library = library;
    this.map = map;
    image = FileUtilities.downloadImageFile(url, library.getImageFolder());
    this.width = width;
    this.height = height;
    Logger.info(
        String.format("Initialized Image at Map ID %d (Source: %s)", map, image.toAbsolutePath()));
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Resets a specific map id.
   *
   * @param library the library
   * @param id the id
   */
  public static void resetMap(@NotNull final MediaLibrary library, final int id) {
    library.getHandler().unregisterMap(id);
  }

  /**
   * Deserializes a map image.
   *
   * @param library the library
   * @param deserialize the deserialize
   * @return the map image
   */
  @NotNull
  public static DynamicImage deserialize(
      @NotNull final MediaLibrary library, @NotNull final Map<String, Object> deserialize) {
    return new DynamicImage(
        library,
        NumberConversions.toInt(deserialize.get("map")),
        String.valueOf(deserialize.get("image")),
        NumberConversions.toInt(deserialize.get("width")),
        NumberConversions.toInt(deserialize.get("height")));
  }

  @Override
  public void drawImage() {
    onDrawImage();
    try {
      final Dimension dims = VideoUtilities.getDimensions(image);
      final int w = (int) dims.getWidth();
      final VLCPlayer player =
          VLCPlayer.builder()
              .url(image.toAbsolutePath().toString())
              .width(w)
              .height((int) dims.getHeight())
              .callback(
                  MapDataCallback.builder()
                      .viewers(null)
                      .map(map)
                      .itemframeWidth(width)
                      .itemframeHeight(height)
                      .videoWidth(w)
                      .delay(0)
                      .ditherHolder(DitherSetting.FLOYD_STEINBERG_DITHER.getHolder())
                      .build(library))
              .build(library);
      player.setRepeat(true);
      player.start(Bukkit.getOnlinePlayers());
      Logger.info(
          String.format(
              "Drew Dynamic Image at Map ID %d (Source: %s)", map, image.toAbsolutePath()));
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDrawImage() {}

  @Override
  @NotNull
  public Map<String, Object> serialize() {
    return ImmutableMap.of(
        "type", "dynamic",
        "map", map,
        "image", image.toAbsolutePath().toString(),
        "width", width,
        "height", height);
  }

  @Override
  public MediaLibrary getLibrary() {
    return library;
  }

  @Override
  public int getMap() {
    return map;
  }

  @Override
  public Path getImage() {
    return image;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getWidth() {
    return width;
  }

  /** Converts a Gif into an MPEG file. */
  private void convertGifIntoMpeg() {}

  /** The type Builder. */
  public static class Builder {

    private int map;
    private Path image;
    private int height;
    private int width;

    private Builder() {}

    /**
     * Sets map.
     *
     * @param map the map
     * @return the map
     */
    public Builder map(final int map) {
      this.map = map;
      return this;
    }

    /**
     * Sets image.
     *
     * @param image the image
     * @return the image
     */
    public Builder image(@NotNull final Path image) {
      this.image = image;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder height(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder width(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Create image map map image.
     *
     * @param library the library
     * @return the map image
     */
    public DynamicImageProxy build(final MediaLibrary library) {
      return new DynamicImage(library, map, image, width, height);
    }
  }
}
