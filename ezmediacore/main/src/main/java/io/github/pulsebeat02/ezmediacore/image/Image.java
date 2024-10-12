/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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

import io.github.pulsebeat02.ezmediacore.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.graphics.ImageUtils;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.bukkit.map.MapView;


public abstract class Image implements MapImage {

  private final transient EzMediaCore core;
  private final transient EnhancedMapRenderer renderer;
  private final Dimension dimension;
  private final List<Integer> maps;
  private final Path image;
  private final UUID uuid;

  public Image(
       final EzMediaCore core,
       final Path image,
       final List<Integer> maps,
       final Dimension dimension) {
    checkNotNull(core, "EzMediaCore cannot be null!");
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
  public  BufferedImage[][] process( BufferedImage image, final boolean resize) {
    if (resize) {
      image = this.resizeImage(image);
    }
    return this.processImage(image);
  }

  private  BufferedImage resizeImage( final BufferedImage image) {
    return ImageUtils.resize(
        image, this.dimension.getWidth() << 7, this.dimension.getHeight() << 7);
  }

  private  BufferedImage[][] processImage( final BufferedImage image) {
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
      final MapView view = requireNonNull(this.core.getPlugin().getServer().getMap(map));
      view.getRenderers().clear();
    }
  }

  @Override
  public  List<Integer> getMaps() {
    return this.maps;
  }

  @Override
  public  Path getImagePath() {
    return this.image;
  }

  @Override
  public  Dimension getDimensions() {
    return this.dimension;
  }

  @Override
  public  EzMediaCore getCore() {
    return this.core;
  }

  @Override
  public  MapRenderer getRenderer() {
    return this.renderer;
  }

  @Override
  public  UUID getIdentifier() {
    return this.uuid;
  }
}
