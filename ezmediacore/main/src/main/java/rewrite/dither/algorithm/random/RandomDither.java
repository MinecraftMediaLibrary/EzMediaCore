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
package rewrite.dither.algorithm.random;

import static java.util.Objects.requireNonNull;

import com.sun.jna.Pointer;
import rewrite.dither.algorithm.ForeignDitherAlgorithm;
import rewrite.dither.load.ColorPalette;
import rewrite.natives.DitherLibC;
import io.github.pulsebeat02.ezmediacore.utility.graphics.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.random.RandomGenerator;



public final class RandomDither extends ForeignDitherAlgorithm {

  public static final int LIGHT_WEIGHT = 32;
  public static final int NORMAL_WEIGHT = 64;
  public static final int HEAVY_WEIGHT = 128;
  private static final RandomGenerator RANDOM = new Xoroshiro128PlusRandom();

  private final int weight;
  private final int min;
  private final int max;

  public RandomDither(final ColorPalette palette, final int weight, final boolean useNative) {
    super(palette, useNative);
    this.weight = weight;
    this.min = -weight;
    this.max = weight + 1;
  }

  public RandomDither(final ColorPalette palette, final int weight) {
    this(palette, weight, false);
  }

  @Override
  public byte[] standardMinecraftDither(
      final int  [] buffer, final int width) {
    final ColorPalette palette = this.getPalette();
    final int length = buffer.length;
    final int height = length / width;
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        r = (r += this.random()) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.random()) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.random()) > 255 ? 255 : Math.max(b, 0);
        data.setByte(index, DitherUtils.getBestColor(palette, r, g, b));
      }
    }
    return data.array();
  }

  @Override
  public void dither(final int  [] buffer, final int width) {
    final ColorPalette palette = this.getPalette();
    final int height = buffer.length / width;
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color) & 0xFF;
        r = (r += this.random()) > 255 ? 255 : Math.max(r, 0);
        g = (g += this.random()) > 255 ? 255 : Math.max(g, 0);
        b = (b += this.random()) > 255 ? 255 : Math.max(b, 0);
        buffer[index] = DitherUtils.getBestColorNormal(palette, r, g, b);
      }
    }
  }

  @Override
  public byte[] ditherIntoMinecraftNatively(
      final int  [] buffer, final int width) {
    final ColorPalette palette = this.getPalette();
    final int[] full = palette.getFullColorMap();
    final byte[] colors = palette.getColorMap();
    final DitherLibC library = requireNonNull(DitherLibC.INSTANCE);
    final Pointer pointer =
        library.randomDither(full, colors, buffer, width, this.weight);
    final byte[] array = pointer.getByteArray(0L, buffer.length);
    final ByteBuf data = Unpooled.buffer(array.length);
    return data.array();
  }

  private int random() {
    return RANDOM.nextInt(this.min, this.max);
  }
}
