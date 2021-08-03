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
import io.github.pulsebeat02.ezmediacore.utility.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class PictureConfiguration extends ConfigurationProvider {

  private final Set<Image> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "configuration/picture.yml");
    this.images = new HashSet<>();
  }

  public void addNormalPhoto(
      final List<Integer> maps, @NotNull final Path file, final int width, final int height)
      throws IOException {
    this.images.add(
        new StaticImage(
            this.getPlugin().library(), file, maps, new ImmutableDimension(width, height)));
  }

  public void addGif(
      final List<Integer> maps, @NotNull final Path file, final int width, final int height)
      throws IOException {
    this.images.add(
        new DynamicImage(
            this.getPlugin().library(), file, maps, new ImmutableDimension(width, height)));
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = this.getFileConfiguration();
    for (final Image image : this.images) {
      final UUID uuid = image.getIdentifier();
      configuration.set(String.format("%s.path", uuid), image.getImagePath());
      configuration.set(String.format("%s.maps", uuid), image.getMaps());
      configuration.set(String.format("%s.dimension", uuid), image.getDimensions());
    }
    this.saveConfig();
  }

  @Override
  public void serialize() throws IOException {
    final FileConfiguration configuration = this.getFileConfiguration();
    final MediaLibraryCore library = this.getPlugin().library();
    for (final String key : configuration.getKeys(false)) {
      final UUID uuid = UUID.fromString(key);
      final Path path =
          Paths.get(requireNonNull(configuration.getString(String.format("%s.path", uuid))));
      final List<Integer> maps = configuration.getIntegerList(String.format("%s.maps", uuid));
      final ImmutableDimension dimension =
          requireNonNull(
              configuration.getSerializable(
                  requireNonNull(configuration.getString(String.format("%s.dimension", uuid))),
                  ImmutableDimension.class));

      if (!Files.exists(path)) {
        continue;
      }

      this.images.add(
          PathUtils.getName(path).endsWith("gif")
              ? new StaticImage(library, path, maps, dimension)
              : new DynamicImage(library, path, maps, dimension));
    }
  }

  public Set<Image> getImages() {
    return this.images;
  }
}
