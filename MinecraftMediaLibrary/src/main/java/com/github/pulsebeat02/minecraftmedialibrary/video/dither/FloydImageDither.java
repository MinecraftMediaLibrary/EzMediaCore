/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import com.github.pulsebeat02.minecraftmedialibrary.annotation.Author;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Floyd–Steinberg dithering is an image dithering algorithm first published in 1976 by Robert W.
 * Floyd and Louis Steinberg. It is commonly used by image manipulation software, for example when
 * an image is converted into GIF format that is restricted to a maximum of 256 colors.
 *
 * <p>The algorithm achieves dithering using error diffusion, meaning it pushes (adds) the residual
 * quantization error of a pixel onto its neighboring pixels, to be dealt with later. It spreads the
 * debt out according to the distribution (shown as a map of the neighboring pixels):
 *
 * <p>The pixel indicated with a star (*) indicates the pixel currently being scanned, and the blank
 * pixels are the previously-scanned pixels. The algorithm scans the image from left to right, top
 * to bottom, quantizing pixel values one by one. Each time the quantization error is transferred to
 * the neighboring pixels, while not affecting the pixels that already have been quantized. Hence,
 * if a number of pixels have been rounded downwards, it becomes more likely that the next pixel is
 * rounded upwards, such that on average, the quantization error is close to zero.
 *
 * <p>The diffusion coefficients have the property that if the original pixel values are exactly
 * halfway in between the nearest available colors, the dithered result is a checkerboard pattern.
 * For example, 50% grey data could be dithered as a black-and-white checkerboard pattern. For
 * optimal dithering, the counting of quantization errors should be in sufficient accuracy to
 * prevent rounding errors from affecting the result.
 *
 * <p>In some implementations, the horizontal direction of scan alternates between lines; this is
 * called "serpentine scanning" or boustrophedon transform dithering.
 *
 * <p>See https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
 */
@Author(
    authors = {"PulseBeat_02", "BananaPuncher714", "jetp250"},
    emails = {"brandonli2006ma@gmail.com", "banana@aaaaahhhhhhh.com", "github.com/jetp250"})
public class FloydImageDither implements DitherHolder {

  private static final int[] PALETTE;
  private static final byte[] COLOR_MAP;
  private static final int[] FULL_COLOR_MAP;
  /**
   * What a piece of optimization; Performs incredibly fast Minecraft color conversion and
   * dithering.
   *
   * @author jetp250, BananaPuncher714
   */
  private static final int largest = 0;

  static {
    PALETTE = StaticDitherInitialization.PALETTE;
    COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
    FULL_COLOR_MAP = StaticDitherInitialization.FULL_COLOR_MAP;
  }

  /**
   * Gets largest.
   *
   * @return the largest
   */
  public static int getLargest() {
    return largest;
  }

  /**
   * Get palette int [ ].
   *
   * @return the int [ ]
   */
  public static int[] getPALETTE() {
    return PALETTE;
  }

  /**
   * Get color map byte [ ].
   *
   * @return the byte [ ]
   */
  public static byte[] getColorMap() {
    return COLOR_MAP;
  }

  /**
   * Get full color map int [ ].
   *
   * @return the int [ ]
   */
  public static int[] getFullColorMap() {
    return FULL_COLOR_MAP;
  }

  /**
   * Gets largest color val.
   *
   * @return the largest color val
   */
  public int getLargestColorVal() {
    return largest;
  }

  /**
   * Gets color from minecraft palette.
   *
   * @param val the val
   * @return the color from minecraft palette
   */
  public int getColorFromMinecraftPalette(final byte val) {
    return PALETTE[(val + 256) % 256];
  }

  /**
   * Gets best color including transparent.
   *
   * @param rgb the rgb
   * @return the best color including transparent
   */
  public byte getBestColorIncludingTransparent(final int rgb) {
    return (rgb >>> 24 & 0xFF) == 0 ? 0 : getBestColor(rgb);
  }

  /**
   * Gets best color.
   *
   * @param rgb the rgb
   * @return the best color
   */
  public byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  /**
   * Gets best color.
   *
   * @param red the red
   * @param green the green
   * @param blue the blue
   * @return the best color
   */
  public byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  /**
   * Gets best full color.
   *
   * @param red the red
   * @param green the green
   * @param blue the blue
   * @return the best full color
   */
  public int getBestFullColor(final int red, final int green, final int blue) {
    return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  /**
   * Simplify byte [ ].
   *
   * @param buffer the buffer
   * @return the byte [ ]
   */
  public byte[] simplify(final int[] buffer) {
    final byte[] map = new byte[buffer.length];
    for (int index = 0; index < buffer.length; index++) {
      final int rgb = buffer[index];
      final int red = rgb >> 16 & 0xFF;
      final int green = rgb >> 8 & 0xFF;
      final int blue = rgb & 0xFF;
      final byte ptr = getBestColor(red, green, blue);
      map[index] = ptr;
    }
    return map;
  }

  /**
   * Dithers buffer data.
   *
   * @param buffer data for the image
   * @param width units for the image
   */
  @Override
  public void dither(final int[] buffer, final int width) {
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
          final boolean hasPrevX = x > 0;
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_r);
            buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
              buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
            }
            buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
            buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_r);
              buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
            }
          }
          buffer[index] = closest;
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasPrevX = x < widthMinus;
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_b);
            buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
              buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
            }
            buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
            buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_b);
              buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
            }
          }
          buffer[index] = closest;
        }
      }
    }
  }

  /**
   * Dithers buffer data into Minecraft.
   *
   * @param buffer data for the image
   * @param width units for the image
   * @return dithered buffer data
   */
  @Override
  public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; y++) {
      final boolean hasNextY = y < heightMinus;
      final int yIndex = y * width;
      if ((y & 0x1) == 0) {
        int bufferIndex = 0;
        final int[] buf1 = dither_buffer[0];
        final int[] buf2 = dither_buffer[1];
        for (int x = 0; x < width; x++) {
          final boolean hasPrevX = x > 0;
          final boolean hasNextX = x < widthMinus;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_r);
            buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
              buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
            }
            buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
            buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_r);
              buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
            }
          }
          data.put(index, getBestColor(closest));
        }
      } else {
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = dither_buffer[1];
        final int[] buf2 = dither_buffer[0];
        for (int x = width - 1; x >= 0; x--) {
          final boolean hasPrevX = x < widthMinus;
          final boolean hasNextX = x > 0;
          final int index = yIndex + x;
          final int rgb = buffer[index];
          int red = rgb >> 16 & 0xFF;
          int green = rgb >> 8 & 0xFF;
          int blue = rgb & 0xFF;
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);
          if (hasNextX) {
            buf1[bufferIndex] = (int) (0.4375 * delta_b);
            buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
            buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
          }
          if (hasNextY) {
            if (hasPrevX) {
              buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
              buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
              buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
            }
            buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
            buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
            buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
            if (hasNextX) {
              buf2[bufferIndex] = (int) (0.0625 * delta_b);
              buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
              buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
            }
          }
          data.put(index, getBestColor(closest));
        }
      }
    }
    return data;
  }

  /**
   * Gets current DitherSetting.
   *
   * @return setting
   */
  @Override
  public DitherSetting getSetting() {
    return DitherSetting.FLOYD_STEINBERG_DITHER;
  }

  /**
   * To buffered image buffered image.
   *
   * @param img the img
   * @return the buffered image
   */
  public BufferedImage toBufferedImage(final @NotNull Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }
    final BufferedImage bimage =
        new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    final Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();
    return bimage;
  }

  /**
   * Get rgb array int [ ].
   *
   * @param image the image
   * @return the int [ ]
   */
  public int[] getRGBArray(final @NotNull BufferedImage image) {
    return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
  }
}
