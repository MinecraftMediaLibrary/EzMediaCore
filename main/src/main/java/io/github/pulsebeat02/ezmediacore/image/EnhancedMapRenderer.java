package io.github.pulsebeat02.ezmediacore.image;

import io.github.pulsebeat02.ezmediacore.dimension.ImmutableDimension;
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
      @NotNull final ImmutableDimension dimension, @NotNull final List<Integer> maps) {
    final int length = dimension.height();
    final int width = dimension.width();
    this.maps = new MapView[length][width];
    int count = 0;
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < width; j++, count++) {
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
        final int x = i;
        final int y = j;
        view.addRenderer(
            new org.bukkit.map.MapRenderer() {
              @Override
              public void render(
                  @NotNull final MapView map,
                  @NotNull final MapCanvas canvas,
                  @NotNull final Player player) {
                canvas.drawImage(0, 0, images[x][y]);
              }
            });
      }
    }
  }
}
