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
package rewrite.dither.algorithm.simple;

import static java.util.Objects.requireNonNull;

import com.sun.jna.Pointer;
import rewrite.dither.algorithm.ForeignDitherAlgorithm;
import rewrite.dither.load.ColorPalette;
import rewrite.natives.DitherLibC;
import io.github.pulsebeat02.ezmediacore.utility.graphics.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class SimpleDither extends ForeignDitherAlgorithm {

  public SimpleDither(final ColorPalette palette, final boolean useNative) {
    super(palette, useNative);
  }

  public SimpleDither() {
    super();
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
        final int color = buffer[yIndex + x];
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color) & 0xFF;
        data.writeByte(DitherUtils.getBestColor(palette, r, g, b));
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
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = (color) & 0xFF;
        buffer[index] = DitherUtils.getBestColorNormal(palette, r, g, b);
      }
    }
  }

  @Override
  public byte[] ditherIntoMinecraftNatively(
      final int[] buffer, final int width) {
    final ColorPalette palette = this.getPalette();
    final int[] full = palette.getFullColorMap();
    final byte[] colors = palette.getColorMap();
    final DitherLibC library = requireNonNull(DitherLibC.INSTANCE);
    final Pointer pointer = library.simpleDither(full, colors, buffer, width);
    final byte[] array = pointer.getByteArray(0L, buffer.length);
    final ByteBuf data = Unpooled.buffer(array.length);
    return data.array();
  }
}
