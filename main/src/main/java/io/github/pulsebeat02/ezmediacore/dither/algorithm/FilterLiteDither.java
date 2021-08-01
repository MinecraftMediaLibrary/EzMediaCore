package io.github.pulsebeat02.ezmediacore.dither.algorithm;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import java.nio.ByteBuffer;

import static io.github.pulsebeat02.ezmediacore.dither.DitherLookupUtil.COLOR_MAP;
import static io.github.pulsebeat02.ezmediacore.dither.DitherLookupUtil.FULL_COLOR_MAP;

public class FilterLiteDither implements DitherAlgorithm {

  /**
   * Performs Filter Lite Dithering at a more optimized pace while giving similar results to Floyd
   * Steinberg Dithering.
   *
   * @author PulseBeat_02
   */
  @Override
  public void dither(final int[] buffer, final int width) {
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

    for (int y = 0; y < height; ++y) {
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
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = getBestFullColor(red, green, blue);
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
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = getBestFullColor(red, green, blue);
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
  public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width << 2];
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; ++y) {
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
          red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
          green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
          blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
          final int closest = getBestFullColor(red, green, blue);
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
          data.put(index, getBestColor(closest));
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
          blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
          green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
          red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
          final int closest = getBestFullColor(red, green, blue);
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
          data.put(index, getBestColor(closest));
        }
      }
    }
    return data;
  }

  private int getBestFullColor(final int red, final int green, final int blue) {
    return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  private byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }
}
