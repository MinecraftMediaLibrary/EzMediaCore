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

package io.github.pulsebeat02.ezmediacore.dither.load;

import java.io.Serial;
import java.util.concurrent.RecursiveTask;

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
