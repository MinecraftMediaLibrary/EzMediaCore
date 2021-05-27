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

package io.github.pulsebeat02.minecraftmedialibrary.frame.dither;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * A class dither implementation which uses a normal get nearest color function. Fast but bad
 * quality.
 */
public final class StandardDithering implements DitherHolder {

  private static final byte[] COLOR_MAP;

  static {
    COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
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
   * @param red the red
   * @param green the green
   * @param blue the blue
   * @return the best color
   */
  public byte getBestColor(final int red, final int green, final int blue) {
    return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
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
        buffer[index] = getBestColorNormal(color);
      }
    }
  }

  /**
   * Dithers buffer data into Minecraft.
   *
   * @param buffer data for the image
   * @param width units for the image
   * @return dithered buffer
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
        data.put(getBestColor(color));
      }
    }
    return data;
  }

  @Override
  @NotNull
  public DitherSetting getSetting() {
    return DitherSetting.STANDARD_MINECRAFT_DITHER;
  }
}
