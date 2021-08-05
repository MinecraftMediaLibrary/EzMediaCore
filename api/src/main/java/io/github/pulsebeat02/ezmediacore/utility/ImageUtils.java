package io.github.pulsebeat02.ezmediacore.utility;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.NotNull;

public final class ImageUtils {

  private ImageUtils() {
  }

  public static BufferedImage resize(
      @NotNull final BufferedImage img, final int width, final int height) {
    final BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2d = resized.createGraphics();
    g2d.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
    g2d.dispose();
    return resized;
  }

  public static void trimForMapSize(@NotNull final BufferedImage img, final int x, final int y) {
    final Graphics2D gr = img.createGraphics();
    gr.drawImage(img, 0, 0, 128, 128, 128 * y, 128 * x, 128 * y + 128, 128 * x + 128, null);
    gr.dispose();
  }
}
