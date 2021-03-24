/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import java.nio.ByteBuffer;

/**
 * The Sierra filters
 *
 * <p>In 1989, Frankie Sierra came out with his three-line filter:
 *
 * <p>* 5 3 The Sierra3 filter 2 4 5 4 2 2 3 2 (1/32)
 *
 * <p>A year later, Sierra followed up with a two-line modification:
 *
 * <p>* 4 3 The Sierra2 filter 1 2 3 2 1 (1/16)
 *
 * <p>and a very simple "Filter Lite," as he calls it:
 *
 * <p>* 2 The Sierra-2-4A filter 1 1 (1/4)
 *
 * <p>Even this very simple filter, according to Sierra, produces better results than the original
 * Floyd-Steinberg filter.
 *
 * <p>See
 * https://web.archive.org/web/20190316064436/http://www.efg2.com/Lab/Library/ImageProcessing/DHALF.TXT
 * for more information about the studies
 */
public class FilterLiteDither implements DitherHolder {

  /**
   * Performs Filter Lite Dithering at a more optimized pace while giving similar results to Floyd
   * Steinberg Dithering.
   *
   * @author PulseBeat_02
   */
  private static final byte[] COLOR_MAP;

  private static final int[] FULL_COLOR_MAP;

  static {
    COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
    FULL_COLOR_MAP = StaticDitherInitialization.FULL_COLOR_MAP;
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

  /** Init. */
  public static void init() {}

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

  /**
   * Gets current DitherSetting.
   *
   * @return setting
   */
  @Override
  public DitherSetting getSetting() {
    return DitherSetting.SIERRA_FILTER_LITE_DITHER;
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
   * Gets best color.
   *
   * @param rgb the rgb
   * @return the best color
   */
  public byte getBestColor(final int rgb) {
    return COLOR_MAP[
        (rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
  }
}
