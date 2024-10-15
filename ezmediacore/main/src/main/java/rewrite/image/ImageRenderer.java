package rewrite.image;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public final class ImageRenderer extends MapRenderer {

  private final BufferedImage[][] images;
  private final int x;
  private final int y;

  public ImageRenderer(final BufferedImage[][] images, final int x, final int y) {
    this.images = images;
    this.x = x;
    this.y = y;
  }

  @Override
  public void render(@NotNull final MapView map, final MapCanvas canvas, @NotNull final Player player) {
    final BufferedImage image = this.images[this.x][this.y];
    canvas.drawImage(0, 0, image);
  }
}
