/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.image;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.graphics.ImageUtils;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public abstract class Image implements MapImage {

  private final transient MediaLibraryCore core;
  private final transient EnhancedMapRenderer renderer;
  private final Dimension dimension;
  private final List<Integer> maps;
  private final Path image;
  private final UUID uuid;

  public Image(
      @NotNull final MediaLibraryCore core,
      @NotNull final Path image,
      @NotNull final List<Integer> maps,
      @NotNull final Dimension dimension) {
    checkNotNull(core, "MediaLibraryCore cannot be null!");
    checkNotNull(image, "Image cannot be null!");
    checkNotNull(maps, "Maps cannot be null!");
    checkNotNull(dimension, "Dimensions cannot be null!");
    checkArgument(maps.size() >= 1, "Invalid Map Matrix!");
    checkArgument(
        maps.size() == dimension.getWidth() * dimension.getHeight(),
        "Maps specified to use doesn't match dimensions (in itemframes) of image!");
    this.core = core;
    this.renderer = new EnhancedMapRenderer(dimension, maps);
    this.image = image;
    this.maps = maps;
    this.dimension = dimension;
    this.uuid = UUID.randomUUID();
  }

  @Override
  public void onStartDrawImage() {}

  @Override
  public @NotNull BufferedImage[][] process(@NotNull BufferedImage image, final boolean resize) {
    if (resize) {
      image = this.resizeImage(image);
    }
    return this.processImage(image);
  }

  private @NotNull BufferedImage resizeImage(@NotNull final BufferedImage image) {
    return ImageUtils.resize(
        image, this.dimension.getWidth() << 7, this.dimension.getHeight() << 7);
  }

  private @NotNull BufferedImage[][] processImage(@NotNull final BufferedImage image) {
    final int width = this.dimension.getWidth();
    final int height = this.dimension.getHeight();
    final BufferedImage[][] matrix = new BufferedImage[width][height];
    for (int rows = 0; rows < matrix.length; rows++) {
      for (int cols = 0; cols < matrix[rows].length; cols++) {
        matrix[rows][cols] = new BufferedImage(128, 128, image.getType());
        ImageUtils.trimForMapSize(matrix[rows][cols], rows, cols);
      }
    }
    return matrix;
  }

  @Override
  public void onFinishDrawImage() {}

  @Override
  public void resetMaps() {
    for (final int map : this.maps) {
      //noinspection deprecation
      requireNonNull(
              this.core.getPlugin().getServer().getMap(map), "Invalid map view %s".formatted(map))
          .getRenderers()
          .clear();
    }
  }

  @Override
  public @NotNull List<Integer> getMaps() {
    return this.maps;
  }

  @Override
  public @NotNull Path getImagePath() {
    return this.image;
  }

  @Override
  public @NotNull Dimension getDimensions() {
    return this.dimension;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull MapRenderer getRenderer() {
    return this.renderer;
  }

  @Override
  public @NotNull UUID getIdentifier() {
    return this.uuid;
  }
}
