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

import io.github.pulsebeat02.ezmediacore.dimension.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public class EnhancedMapRenderer implements MapRenderer {

  private final MapView[][] maps;

  public EnhancedMapRenderer(
      @NotNull final Dimension dimension, @NotNull final List<Integer> maps) {
    this.maps = new MapView[dimension.getHeight()][dimension.getWidth()];
    this.fillMaps(maps, dimension);
  }

  private void fillMaps(@NotNull final List<Integer> maps, @NotNull final Dimension dimension) {
    final int length = dimension.getHeight();
    final int width = dimension.getWidth();
    int count = 0;
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < width; j++, count++) {
        //noinspection deprecation
        this.maps[i][j] = Bukkit.getMap(maps.get(count));
      }
    }
  }

  @Override
  public void drawMap(@NotNull final BufferedImage[][] images) {
    for (int i = 0; i < this.maps.length; i++) {
      for (int j = 0; j < this.maps[i].length; j++) {
        final MapView view = this.maps[i][j];
        view.getRenderers().clear();
        view.addRenderer(this.createRenderer(images, i, j));
      }
    }
  }

  private org.bukkit.map.MapRenderer createRenderer(
      @NotNull final BufferedImage[][] images, final int x, final int y) {
    return new org.bukkit.map.MapRenderer() {
      @Override
      public void render(
          @NotNull final MapView map,
          @NotNull final MapCanvas canvas,
          @NotNull final Player player) {
        canvas.drawImage(0, 0, images[x][y]);
      }
    };
  }
}
