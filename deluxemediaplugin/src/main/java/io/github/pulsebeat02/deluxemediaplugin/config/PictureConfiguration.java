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
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.image.DynamicImage;
import io.github.pulsebeat02.ezmediacore.image.Image;
import io.github.pulsebeat02.ezmediacore.image.StaticImage;
import io.github.pulsebeat02.ezmediacore.utility.ImmutableDimension;
import io.github.pulsebeat02.minecraftmedialibrary.image.basic.StaticImageProxy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class PictureConfiguration extends ConfigurationProvider {

  private final Set<Image> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/picture.yml");
    images = new HashSet<>();
  }

  public void addNormalPhoto(
      final int[][] maps, @NotNull final Path file, final int width, final int height)
      throws IOException {
    images.add(
        new StaticImage(getPlugin().library(), file, maps, new ImmutableDimension(width, height)));
  }

  public void addGif(
      final int[][] maps, @NotNull final Path file, final int width, final int height)
      throws IOException {
    images.add(
        new DynamicImage(getPlugin().library(), file, maps, new ImmutableDimension(width, height)));
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    for (final Image image : images) {
      final Path key = image.getImagePath();
      configuration.set(String.format("%d.maps", key), image.getMapMatrix());
      configuration.set(String.format("%d.dimension", key), image.getDimensions());
    }
    saveConfig();
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    final MediaLibraryCore library = getPlugin().library();
    for (final String key : configuration.getKeys(false)) {
      final Path path = Paths.get(key);

      if (!Files.exists(path)) {
        continue;
      }

      final int width = configuration.getInt(String.format("%d.width", id));
      final int height = configuration.getInt(String.format("%d.height", id));
      images.add(
          StaticImage.builder().map(id).image(file).width(width).height(height).build(library));
    }
  }

  public Set<StaticImageProxy> getImages() {
    return images;
  }
}
