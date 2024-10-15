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
package rewrite.image;

import static java.util.Objects.requireNonNull;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.map.MapRenderer;
import rewrite.dimension.Dimension;
import io.github.pulsebeat02.ezmediacore.utility.graphics.ImageUtils;
import java.awt.image.BufferedImage;
import java.util.List;

import org.bukkit.map.MapView;

public final class StaticImage {

  private final Dimension dimension;
  private final List<Integer> maps;
  private final DitheredImage image;

  public StaticImage(
       final DitheredImage image,
       final List<Integer> maps,
       final Dimension dimension) {
    this.image = image;
    this.maps = maps;
    this.dimension = dimension;
  }

  public void drawImage(final boolean resize) {
    final BufferedImage internal = this.image.getInternal();
    final BufferedImage img = resize ? this.resizeImage(internal) : internal;
    final BufferedImage[][] divided = this.processImage(img);
    final int length = this.dimension.getHeight();
    final int width = this.dimension.getWidth();
    int count = 0;
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < width; j++, count++) {
        final int id = this.maps.get(count);
        final MapView view = requireNonNull(Bukkit.getMap(id));
        final MapRenderer custom = new ImageRenderer(divided, i, j);
        final List<MapRenderer> renderer = view.getRenderers();
        renderer.clear();
        view.addRenderer(custom);
      }
    }
  }

  public void resetMaps() {
    final Server server = Bukkit.getServer();
    for (final int map : this.maps) {
      final MapView view = requireNonNull(server.getMap(map));
      final List<MapRenderer> renderers = view.getRenderers();
      renderers.clear();
    }
  }

  private BufferedImage resizeImage(final BufferedImage image) {
    final int widthBlocks = this.dimension.getWidth() << 7;
    final int heightBlocks = this.dimension.getHeight() << 7;
    return ImageUtils.resize(image, widthBlocks, heightBlocks);
  }

  private BufferedImage[][] processImage(final BufferedImage image) {
    final int width = this.dimension.getWidth();
    final int height = this.dimension.getHeight();
    final BufferedImage[][] matrix = new BufferedImage[width][height];
    final int type = image.getType();
    for (int rows = 0; rows < matrix.length; rows++) {
      for (int cols = 0; cols < matrix[rows].length; cols++) {
        final BufferedImage bufferedImage = new BufferedImage(128, 128, type);
        ImageUtils.trimForMapSize(bufferedImage, rows, cols);
        matrix[rows][cols] = bufferedImage;
      }
    }
    return matrix;
  }
}
