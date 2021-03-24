/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Ordered dithering is an image dithering algorithm. It is commonly used to display a continuous
 * image on a display of smaller color depth. For example, Microsoft Windows uses it in 16-color
 * graphics modes. The algorithm is characterized by noticeable crosshatch patterns in the result.
 *
 * <p>The algorithm reduces the number of colors by applying a threshold map M to the pixels
 * displayed, causing some pixels to change color, depending on the distance of the original color
 * from the available color entries in the reduced palette.
 *
 * <p>The ordered dithering algorithm renders the image normally, but for each pixel, it offsets its
 * color value with a corresponding value from the threshold map according to its location, causing
 * the pixel's value to be quantized to a different color if it exceeds the threshold.
 *
 * <p>For most dithering purposes, it is sufficient to simply add the threshold value to every
 * pixel, or equivalently, to compare that pixel's value to the threshold: if the value of a pixel
 * is less than the number in the corresponding cell of the matrix, plot that pixel black,
 * otherwise, plot it white.
 *
 * <p>This slightly increases the average brightness of the image, and causes almost-white pixels to
 * not be dithered. This is not a problem when using a gray scale palette (or any palette where the
 * relative color distances are (nearly) constant), and it is often even desired, since the human
 * eye perceives differences in darker colors more accurately than lighter ones, however, it
 * produces incorrect results especially when using a small or arbitrary palette, so proper
 * offsetting should be preferred...
 *
 * <p>See https://en.wikipedia.org/wiki/Ordered_dithering
 */
public class OrderedDithering implements DitherHolder {

  /**
   * Performs Ordered Dithering with a selection of matrices to choose from.
   *
   * @author PulseBeat_02
   */
  private static final byte[] COLOR_MAP;

  private static final float[][] bayerMatrixTwo;
  private static final float[][] bayerMatrixFour;
  private static final float[][] bayerMatrixEight;

  static {
    COLOR_MAP = StaticDitherInitialization.COLOR_MAP;

    /*

    2 by 2 Bayer Ordered Dithering

    0   2
    3   1  (1/4)

    */

    bayerMatrixTwo =
        new float[][] {
          {1f, 3f},
          {4f, 2f},
        };

    /*

    4 by 4 Bayer Ordered Dithering

    1  9  3  11
    13 5  15  7
    4  12  2  10
    16 8  14  6   (1/16)

     */

    bayerMatrixFour =
        new float[][] {
          {1f, 9f, 3f, 11f},
          {13f, 5f, 15f, 7f},
          {4f, 12f, 2f, 10f},
          {16f, 8f, 14f, 6f}
        };

    /*

    8 by 8 Bayer Ordered Dithering

    1  49  13  61  4  52  16  64
    33 17  45  29  36  20  48  32
    9  57  5  53  12  60  8  56
    41  25  37  21  44  28  40  24
    3  51  15  63  2  50  14  62
    35  19  47  31  34  18  46  30
    11  59  7  55  10  58  6  54
    43  27  39  23  42  26  38  22   (1/64)

     */

    bayerMatrixEight =
        new float[][] {
          {1f, 49f, 13f, 61f, 4f, 52f, 16f, 64f},
          {33f, 17f, 45f, 29f, 36f, 20f, 48f, 32f},
          {9f, 57f, 5f, 53f, 12f, 60f, 8f, 56f},
          {41f, 25f, 37f, 21f, 44f, 28f, 40f, 24f},
          {3f, 51f, 15f, 63f, 2f, 50f, 14f, 62f},
          {35f, 19f, 47f, 31f, 34f, 18f, 46f, 30f},
          {11f, 59f, 7f, 55f, 10f, 58f, 6f, 54f},
          {43f, 27f, 39f, 23f, 42f, 26f, 38f, 22f}
        };
  }

  private final float r;
  private float[][] matrix;
  private float multiplicative;
  private int n;

  /**
   * Instantiates a new Ordered dithering.
   *
   * @param type the type
   */
  public OrderedDithering(@NotNull final DitherType type) {
    switch (type) {
      case ModeTwo:
        matrix = bayerMatrixTwo;
        n = 2;
        multiplicative = 0.25f;
        break;
      case ModeFour:
        matrix = bayerMatrixFour;
        n = 4;
        multiplicative = 0.0625f;
        break;
      case ModeEight:
        matrix = bayerMatrixEight;
        n = 8;
        multiplicative = 0.015625f;
        break;
    }
    r = 255f / (n * n);
    convertToFloat();
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
   * Get bayer matrix two float [ ] [ ].
   *
   * @return the float [ ] [ ]
   */
  public static float[][] getBayerMatrixTwo() {
    return bayerMatrixTwo;
  }

  /**
   * Get bayer matrix four float [ ] [ ].
   *
   * @return the float [ ] [ ]
   */
  public static float[][] getBayerMatrixFour() {
    return bayerMatrixFour;
  }

  /**
   * Get bayer matrix eight float [ ] [ ].
   *
   * @return the float [ ] [ ]
   */
  public static float[][] getBayerMatrixEight() {
    return bayerMatrixEight;
  }

  /**
   * Gets best color normal.
   *
   * @param rgb the rgb
   * @return the best color normal
   */
  public int getBestColorNormal(final int rgb) {
    return MinecraftMapPalette.getColor(getBestColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF))
        .getRGB();
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

  /** Convert to float. */
  public void convertToFloat() {
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        matrix[i][j] = matrix[i][j] * multiplicative - 0.5f;
      }
    }
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
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        buffer[index] = getBestColorNormal((int) (color + r * (matrix[x % n][y % n])));
      }
    }
  }

  /**
   * Dithers buffer data into Minecraft.
   *
   * @param buffer data for the image
   * @param width units for the image
   * @return dithered buffer.
   */
  @Override
  public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
    final int height = buffer.length / width;
    final ByteBuffer data = ByteBuffer.allocate(buffer.length);
    for (int y = 0; y < height; y++) {
      final int yIndex = y * width;
      for (int x = 0; x < width; x++) {
        final int index = yIndex + x;
        final int color = buffer[index];
        data.put(getBestColor((int) (color + r * (matrix[x % n][y % n]))));
      }
    }
    return data;
  }

  /**
   * Gets the current DitherSetting.
   *
   * @return setting
   */
  @Override
  public DitherSetting getSetting() {
    switch (n) {
      case 2:
        return DitherSetting.BAYER_ORDERED_2_DIMENSIONAL;
      case 4:
        return DitherSetting.BAYER_ORDERED_4_DIMENSIONAL;
      case 8:
        return DitherSetting.BAYER_ORDERED_8_DIMENSIONAL;
    }
    return null;
  }

  /**
   * Get matrix float [ ] [ ].
   *
   * @return the float [ ] [ ]
   */
  public float[][] getMatrix() {
    return matrix;
  }

  /**
   * Gets r.
   *
   * @return the r
   */
  public float getR() {
    return r;
  }

  /**
   * Gets multiplicative.
   *
   * @return the multiplicative
   */
  public float getMultiplicative() {
    return multiplicative;
  }

  /**
   * Gets n.
   *
   * @return the n
   */
  public int getN() {
    return n;
  }

  public enum DitherType {

    /** Two Dimensional */
    ModeTwo("Two Dimensional"),
    /** Four Dimensional */
    ModeFour("Four Dimensional"),
    /** Eight Dimensional */
    ModeEight("Eight Dimensional");

    private final String name;

    DitherType(@NotNull final String name) {
      this.name = name;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
      return name;
    }
  }
}
