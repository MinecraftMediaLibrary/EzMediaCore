package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import java.nio.ByteBuffer;

public class PulseDithering implements AbstractDitherHolder {

  /**
   * Performs Filter Lite Dithering at a more optimized pace while sacrificing more memory as a
   * result.
   *
   * @author PulseBeat_02
   */
  private static final byte[][][] table;

  private static final byte[] COLOR_MAP;

  private static final int[] FULL_COLOR_MAP;

  static {
    COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
    FULL_COLOR_MAP = StaticDitherInitialization.FULL_COLOR_MAP;
    table = new byte[256][256][256];
    for (int r = 0; r < 256; r++) {
      for (int g = 0; g < 256; g++) {
        for (int b = 0; b < 256; b++) {
          table[r][g][b] = getBestColor(((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff));
        }
      }
    }
  }

  public static byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }

  @Override
  public void dither(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final int widthMinus = width - 1;
    final int heightMinus = height - 1;
    final int[][] dither_buffer = new int[2][width + width << 1];
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
          final int closest = table[red][green][blue];
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
          final int closest = table[red][green][blue];
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
    return null;
  }

  @Override
  public DitherSetting getSetting() {
    return null;
  }
}
