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
package io.github.pulsebeat02.ezmediacore.utility.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.NotNull;

public final class ImageUtils {

  private ImageUtils() {
  }

  public static @NotNull BufferedImage resize(
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
