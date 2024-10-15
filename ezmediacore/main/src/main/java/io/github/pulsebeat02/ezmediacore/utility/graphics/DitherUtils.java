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
package io.github.pulsebeat02.ezmediacore.utility.graphics;

import rewrite.dither.MapPalette;
import rewrite.dither.load.ColorPalette;


public final class DitherUtils {

  private DitherUtils() {}

  public static byte getBestColor(final ColorPalette palette, final int r, final int g, final int b) {
    final byte[] colors = palette.getColorMap();
    return colors[r >> 1 << 14 | g >> 1 << 7 | b >> 1];
  }

  public static int getBestFullColor(final ColorPalette palette, final int red, final int green, final int blue) {
    final int[] colors = palette.getFullColorMap();
    return colors[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  public static int getBestColorNormal(final ColorPalette palette, final int r, final int g, final int b) {
    return MapPalette.getColor(getBestColor(palette, r, g, b)).getRGB();
  }

  public static int getColorFromMinecraftPalette(final ColorPalette palette, final byte val) {
    final int[] colors = palette.getPalette();
    return colors[(val + 256) % 256];
  }

  public static byte getBestColorIncludingTransparent(final ColorPalette palette, final int rgb) {
    final int r = (rgb >> 16) & 0xFF;
    final int g = (rgb >> 8) & 0xFF;
    final int b = rgb & 0xFF;
    return (rgb >>> 24 & 0xFF) == 0 ? 0 : getBestColor(palette, r, g, b);
  }

  public static byte  [] simplify(final ColorPalette palette, final int [] buffer) {
    final byte[] map = new byte[buffer.length];
    for (int index = 0; index < buffer.length; index++) {
      final int rgb = buffer[index];
      final int red = rgb >> 16 & 0xFF;
      final int green = rgb >> 8 & 0xFF;
      final int blue = rgb & 0xFF;
      final byte ptr = DitherUtils.getBestColor(palette, red, green, blue);
      map[index] = ptr;
    }
    return map;
  }
}
