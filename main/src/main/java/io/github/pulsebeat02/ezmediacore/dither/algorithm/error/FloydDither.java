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
package io.github.pulsebeat02.ezmediacore.dither.algorithm.error;

import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.FULL_COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.load.DitherLookupUtil.PALETTE;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.dither.algorithm.NativelySupportedDitheringAlgorithm;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.natives.DitherLibC;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * What a piece of optimization; Performs incredibly fast Minecraft color conversion and dithering.
 *
 * @author jetp250, BananaPuncher714
 */
public final class FloydDither extends NativelySupportedDitheringAlgorithm {

  public FloydDither(final boolean useNative) {
    super(useNative);
  }

  public FloydDither() {
    super();
  }

  private int getColorFromMinecraftPalette(final byte val) {
    return PALETTE[(val + 256) % 256];
  }

  private byte getBestColorIncludingTransparent(final int rgb) {
    return (rgb >>> 24 & 0xFF) == 0 ? 0 : this.getBestColor(rgb);
  }

  private byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  private byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private int getBestFullColor(final int red, final int green, final int blue) {
    return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  @Contract(pure = true)
  private byte @NotNull [] simplify(final int @NotNull [] buffer) {
    final byte[] map = new byte[buffer.length];
    for (int index = 0; index < buffer.length; index++) {
      final int rgb = buffer[index];
      final int red = rgb >> 16 & 0xFF;
      final int green = rgb >> 8 & 0xFF;
      final int blue = rgb & 0xFF;
      final byte ptr = this.getBestColor(red, green, blue);
      map[index] = ptr;
    }
    return map;
  }

  @Override
  public void dither(final int @NotNull [] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : Math.max(red, 0);
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : Math.max(green, 0);
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : Math.max(blue, 0);
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            // 0.4375 -> 7/16
            buf1[bufferIndex] = (delta_r >> 4) * 7;
            buf1[bufferIndex + 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex + 2] = (delta_b >> 4) * 7;
          }
          if (hasNextY) {
            if (x > 0) {
              // 0.1875 -> 3/16
              buf2[bufferIndex - 6] = (delta_r >> 4) * 3;
              buf2[bufferIndex - 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex - 4] = (delta_b >> 4) * 3;
            }
            // 0.3125 -> 5/16
            buf2[bufferIndex - 3] = (delta_r >> 4) * 5;
            buf2[bufferIndex - 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex - 1] = (delta_b >> 4) * 5;
            if (hasNextX) {
              // 0.0625 -> 1/16
              buf2[bufferIndex] = delta_r >> 4;
              buf2[bufferIndex + 1] = delta_g >> 4;
              buf2[bufferIndex + 2] = delta_b >> 4;
            }
          }
          buffer[index] = closest;
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : Math.max(blue, 0);
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : Math.max(green, 0);
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : Math.max(red, 0);
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (delta_b >> 4) * 7;
            buf1[bufferIndex - 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex - 2] = (delta_r >> 4) * 7;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = (delta_b >> 4) * 3;
              buf2[bufferIndex + 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex + 4] = (delta_r >> 4) * 3;
            }
            buf2[bufferIndex + 3] = (delta_b >> 4) * 5;
            buf2[bufferIndex + 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex + 1] = (delta_r >> 4) * 5;
            if (hasNextX) {
              buf2[bufferIndex] = delta_b >> 4;
              buf2[bufferIndex - 1] = delta_g >> 4;
              buf2[bufferIndex - 2] = delta_r >> 4;
            }
          }
          buffer[index] = closest;
        }
      }
    }
  }

  @Override
  public @NotNull BufferCarrier standardMinecraftDither(
      final int @NotNull [] buffer, final int width) {
    final int length = buffer.length;
    final int height = length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    final ByteBuf data = Unpooled.buffer(length);
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : Math.max(red, 0);
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : Math.max(green, 0);
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : Math.max(blue, 0);
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (delta_r >> 4) * 7;
            buf1[bufferIndex + 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex + 2] = (delta_b >> 4) * 7;
          }
          if (hasNextY) {
            if (x > 0) {
              buf2[bufferIndex - 6] = (delta_r >> 4) * 3;
              buf2[bufferIndex - 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex - 4] = (delta_b >> 4) * 3;
            }
            buf2[bufferIndex - 3] = (delta_r >> 4) * 5;
            buf2[bufferIndex - 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex - 1] = (delta_b >> 4) * 5;
            if (hasNextX) {
              buf2[bufferIndex] = delta_r >> 4;
              buf2[bufferIndex + 1] = delta_g >> 4;
              buf2[bufferIndex + 2] = delta_b >> 4;
            }
          }
          data.setByte(index, this.getBestColor(closest));
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : Math.max(blue, 0);
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : Math.max(green, 0);
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : Math.max(red, 0);
          final int closest = this.getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (delta_b >> 4) * 7;
            buf1[bufferIndex - 1] = (delta_g >> 4) * 7;
            buf1[bufferIndex - 2] = (delta_r >> 4) * 7;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = (delta_b >> 4) * 3;
              buf2[bufferIndex + 5] = (delta_g >> 4) * 3;
              buf2[bufferIndex + 4] = (delta_r >> 4) * 3;
            }
            buf2[bufferIndex + 3] = (delta_b >> 4) * 5;
            buf2[bufferIndex + 2] = (delta_g >> 4) * 5;
            buf2[bufferIndex + 1] = (delta_r >> 4) * 5;
            if (hasNextX) {
              buf2[bufferIndex] = delta_b >> 4;
              buf2[bufferIndex - 1] = delta_g >> 4;
              buf2[bufferIndex - 2] = delta_r >> 4;
            }
          }
          data.setByte(index, this.getBestColor(closest));
        }
      }
    }
    return ByteBufCarrier.ofByteBufCarrier(data);
  }

  @Override
  public @NotNull BufferCarrier ditherIntoMinecraftNatively(
      final int @NotNull [] buffer, final int width) {
    return ByteBufCarrier.ofByteBufCarrier(
        Unpooled.wrappedBuffer(
            DitherLibC.INSTANCE
                .floydSteinbergDither(FULL_COLOR_MAP, COLOR_MAP, buffer, width)
                .getByteArray(0L, buffer.length)));
  }

  private int[] getRGBArray(@NotNull final BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }
}
