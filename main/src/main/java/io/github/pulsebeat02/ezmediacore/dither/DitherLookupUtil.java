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
package io.github.pulsebeat02.ezmediacore.dither;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.annotation.Author;
import java.awt.Color;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

@Author(
    authors = {"PulseBeat_02", "BananaPuncher714", "jetp250"},
    emails = {"brandonli2006ma@gmail.com", "banana@aaaaahhhhhhh.com", "github.com/jetp250"})
public class DitherLookupUtil {

  public static final int[] PALETTE;
  public static final byte[] COLOR_MAP = new byte[128 * 128 * 128];
  public static final int[] FULL_COLOR_MAP = new int[128 * 128 * 128];

  static {
    final List<Integer> colors = new ArrayList<>();
    final long start = System.nanoTime();
    for (int i = 0; i < 256; ++i) {
      try {
        final Color color = MapPalette.getColor((byte) i);
        colors.add(color.getRGB());
      } catch (final IndexOutOfBoundsException e) {
        Logger.info("Captured %d colors!".formatted(i - 1));
        break;
      }
    }

    PALETTE = new int[colors.size()];
    int index = 0;
    for (final int color : colors) {
      PALETTE[index++] = color;
    }
    PALETTE[0] = 0;

    final List<LoadRed> tasks = new ArrayList<>(128);
    for (int r = 0; r < 256; r += 2) {
      final LoadRed red = new LoadRed(PALETTE, r);
      tasks.add(red);
      red.fork();
    }

    for (int i = 0; i < 128; i++) {
      final byte[] sub = tasks.get(i).join();
      final int ci = i << 14;
      for (int si = 0; si < 16384; si++) {
        COLOR_MAP[ci + si] = sub[si];
        FULL_COLOR_MAP[ci + si] = PALETTE[Byte.toUnsignedInt(sub[si])];
      }
    }

    Logger.info(
        "Initial lookup table initialized in %s ms"
            .formatted((System.nanoTime() - start) / 1_000_000.0));
  }

  public static int[] getPALETTE() {
    return PALETTE;
  }

  public static byte[] getColorMap() {
    return COLOR_MAP;
  }

  public static int[] getFullColorMap() {
    return FULL_COLOR_MAP;
  }

  /**
   * Init.
   */
  public static void init() {
  }
}

final class LoadRed extends RecursiveTask<byte[]> {

  @Serial
  private static final long serialVersionUID = -6408377810782246185L;
  private final int r;
  private final int[] palette;

  LoadRed(final int[] palette, final int r) {
    this.r = r;
    this.palette = palette;
  }

  @Override
  protected byte[] compute() {
    final List<LoadGreen> greenSub = new ArrayList<>(128);
    for (int g = 0; g < 256; g += 2) {
      final LoadGreen green = new LoadGreen(this.palette, this.r, g);
      greenSub.add(green);
      green.fork();
    }
    final byte[] vals = new byte[16384];
    for (int i = 0; i < 128; i++) {
      final byte[] sub = greenSub.get(i).join();
      final int index = i << 7;
      System.arraycopy(sub, 0, vals, index, 128);
    }
    return vals;
  }
}

final class LoadGreen extends RecursiveTask<byte[]> {

  @Serial
  private static final long serialVersionUID = -1221290051151782146L;
  private final int r;
  private final int g;
  private final int[] palette;

  LoadGreen(final int[] palette, final int r, final int g) {
    this.r = r;
    this.g = g;
    this.palette = palette;
  }

  @Override
  protected byte[] compute() {
    final List<LoadBlue> blueSub = new ArrayList<>(128);
    for (int b = 0; b < 256; b += 2) {
      final LoadBlue blue = new LoadBlue(this.palette, this.r, this.g, b);
      blueSub.add(blue);
      blue.fork();
    }
    final byte[] matches = new byte[128];
    for (int i = 0; i < 128; i++) {
      matches[i] = blueSub.get(i).join();
    }
    return matches;
  }
}

final class LoadBlue extends RecursiveTask<Byte> {

  @Serial
  private static final long serialVersionUID = 5331764784578439634L;
  private final int r, g, b;
  private final int[] palette;

  LoadBlue(final int[] palette, final int r, final int g, final int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.palette = palette;
  }

  @Override
  protected Byte compute() {
    int val = 0;
    float best_distance = Float.MAX_VALUE;
    float distance;
    int col;
    for (int i = 4; i < this.palette.length; ++i) {
      col = this.palette[i];
      final int r2 = col >> 16 & 0xFF;
      final int g2 = col >> 8 & 0xFF;
      final int b2 = col & 0xFF;
      final float red_avg = (this.r + r2) * .5f;
      final int redVal = this.r - r2;
      final int greenVal = this.g - g2;
      final int blueVal = this.b - b2;
      final float weight_red = 2.0f + red_avg * (1f / 256f);
      final float weight_green = 4.0f;
      final float weight_blue = 2.0f + (255.0f - red_avg) * (1f / 256f);
      distance =
          weight_red * redVal * redVal
              + weight_green * greenVal * greenVal
              + weight_blue * blueVal * blueVal;
      if (distance < best_distance) {
        best_distance = distance;
        val = i;
      }
    }
    return (byte) val;
  }
}
