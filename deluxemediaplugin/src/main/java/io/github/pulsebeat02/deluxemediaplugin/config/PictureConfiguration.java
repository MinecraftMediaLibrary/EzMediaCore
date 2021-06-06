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
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImage;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImageProxy;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PictureConfiguration extends ConfigurationProvider {

  private final Set<StaticImageProxy> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "picture.yml");
    images = new HashSet<>();
  }

  public void addPhoto(final int map, @NotNull final Path file, final int width, final int height) {

    // Add an image
    images.add(
        StaticImage.builder()
            .map(map)
            .image(file)
            .width(width)
            .height(height)
            .build(getPlugin().getLibrary()));
  }

  @Override
  public void deserialize() {

    // Deserialize the images settings
    final FileConfiguration configuration = getFileConfiguration();
    for (final StaticImageProxy image : images) {
      final long key = image.getMap();
      configuration.set(String.format("%d.location", key), image.getImage().toString());
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
    final MediaLibrary library = getPlugin().getLibrary();

    for (final String key : configuration.getKeys(false)) {

      // Get the map id
      final int id = Integer.parseInt(key);

      // Get the file path of the image
      final Path file =
          Paths.get(
              Objects.requireNonNull(configuration.getString(String.format("%d.location", id))));

      // If it doesn't exist, throw an error
      if (!Files.exists(file)) {
        Logger.error(String.format("Could not read %s at id %d!", file, id));
        continue;
      }

      // Get the width of the image
      final int width = configuration.getInt(String.format("%d.width", id));

      // Get the height of the image
      final int height = configuration.getInt(String.format("%d.height", id));

      // Define a new image with the specified id
      images.add(
          StaticImage.builder().map(id).image(file).width(width).height(height).build(library));
    }
  }

  public Set<StaticImageProxy> getImages() {
    return images;
  }
}
