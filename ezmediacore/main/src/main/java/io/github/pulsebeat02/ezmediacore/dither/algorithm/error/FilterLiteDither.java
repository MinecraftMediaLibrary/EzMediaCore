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
package io.github.pulsebeat02.ezmediacore.dither.algorithm.error;

import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.FULL_COLOR_MAP;
import static java.util.Objects.requireNonNull;

import com.sun.jna.Pointer;
import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.ForeignDitherAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.natives.DitherLibC;
import io.github.pulsebeat02.ezmediacore.utility.graphics.DitherUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public final class FilterLiteDither extends ForeignDitherAlgorithm {

  public FilterLiteDither(final boolean useNative) {
    super(useNative);
  }

  public FilterLiteDither() {
    super();
  }

  /**
   * Performs Filter Lite Dithering at a more optimized pace while giving similar results to Floyd
   * Steinberg Dithering.
   *
   * @author PulseBeat_02
   */
  @Override
  public void dither(final int  [] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];

    /*

    Simple Sierra 2-4A Dithering (Filter Lite)


        *  2
    1  1       (1/4)

    When Jagged Matrix is Multiplied:

          *  2/4
    1/4 1/4

     */

    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; ++x) {
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : Math.max(red, 0);
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : Math.max(green, 0);
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : Math.max(blue, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (x < widthMinus) {
            buf1[bufferIndex] = delta_r >> 1;
            buf1[bufferIndex + 1] = delta_g >> 1;
            buf1[bufferIndex + 2] = delta_b >> 1;
          }
          if (hasNextY) {
            if (x > 0) {
              buf2[bufferIndex - 6] = delta_r >> 2;
              buf2[bufferIndex - 5] = delta_g >> 2;
              buf2[bufferIndex - 4] = delta_b >> 2;
            }
            buf2[bufferIndex - 3] = delta_r >> 2;
            buf2[bufferIndex - 2] = delta_g >> 2;
            buf2[bufferIndex - 1] = delta_b >> 2;
          }
          buffer[index] = closest;
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; --x) {
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : Math.max(blue, 0);
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : Math.max(green, 0);
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : Math.max(red, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (x > 0) {
            buf1[bufferIndex] = delta_b >> 1;
            buf1[bufferIndex - 1] = delta_g >> 1;
            buf1[bufferIndex - 2] = delta_r >> 1;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = delta_b >> 2;
              buf2[bufferIndex + 5] = delta_g >> 2;
              buf2[bufferIndex + 4] = delta_r >> 2;
            }
            buf2[bufferIndex + 3] = delta_b >> 2;
            buf2[bufferIndex + 2] = delta_g >> 2;
            buf2[bufferIndex + 1] = delta_r >> 2;
          }
          buffer[index] = closest;
        }
      }
    }
  }

  @Override
  public  BufferCarrier standardMinecraftDither(
      final int  [] buffer, final int width) {
    final int length = buffer.length;
    final int height = length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width << 2];
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; ++x) {
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : Math.max(red, 0);
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : Math.max(green, 0);
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : Math.max(blue, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int r = closest >> 16 & 0xFF;
          final int g = closest >> 8 & 0xFF;
          final int b = closest & 0xFF;
          final int delta_r = red - r;
          final int delta_g = green - g;
          final int delta_b = blue - b;
          if (x < widthMinus) {
            buf1[bufferIndex] = delta_r >> 1;
            buf1[bufferIndex + 1] = delta_g >> 1;
            buf1[bufferIndex + 2] = delta_b >> 1;
          }
          if (hasNextY) {
            if (x > 0) {
              buf2[bufferIndex - 6] = delta_r >> 2;
              buf2[bufferIndex - 5] = delta_g >> 2;
              buf2[bufferIndex - 4] = delta_b >> 2;
            }
            buf2[bufferIndex - 3] = delta_r >> 2;
            buf2[bufferIndex - 2] = delta_g >> 2;
            buf2[bufferIndex - 1] = delta_b >> 2;
          }
          data.setByte(index, DitherUtils.getBestColor(r, g, b));
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; --x) {
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : Math.max(blue, 0);
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : Math.max(green, 0);
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : Math.max(red, 0);
          final int closest = DitherUtils.getBestFullColor(red, green, blue);
          final int r = closest >> 16 & 0xFF;
          final int g = closest >> 8 & 0xFF;
          final int b = closest & 0xFF;
          final int delta_r = red - r;
          final int delta_g = green - g;
          final int delta_b = blue - b;
          if (x > 0) {
            buf1[bufferIndex] = delta_b >> 1;
            buf1[bufferIndex - 1] = delta_g >> 1;
            buf1[bufferIndex - 2] = delta_r >> 1;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = delta_b >> 2;
              buf2[bufferIndex + 5] = delta_g >> 2;
              buf2[bufferIndex + 4] = delta_r >> 2;
            }
            buf2[bufferIndex + 3] = delta_b >> 2;
            buf2[bufferIndex + 2] = delta_g >> 2;
            buf2[bufferIndex + 1] = delta_r >> 2;
          }
          data.setByte(index, DitherUtils.getBestColor(r, g, b));
        }
      }
    }
    return ByteBufCarrier.ofByteBufCarrier(data);
  }

  @Override
  public  BufferCarrier ditherIntoMinecraftNatively(
      final int  [] buffer, final int width) {
    final DitherLibC library = requireNonNull(DitherLibC.INSTANCE);
    final Pointer pointer = library.filterLiteDither(FULL_COLOR_MAP, COLOR_MAP, buffer, width);
    final byte[] array = pointer.getByteArray(0L, buffer.length);
    final ByteBuf data = Unpooled.buffer(array.length);
    return ByteBufCarrier.ofByteBufCarrier(data);
  }
}
