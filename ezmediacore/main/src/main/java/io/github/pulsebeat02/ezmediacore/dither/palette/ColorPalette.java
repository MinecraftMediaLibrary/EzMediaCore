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
package io.github.pulsebeat02.ezmediacore.dither.palette;

import io.github.pulsebeat02.ezmediacore.dither.load.LoadRed;

import java.util.ArrayList;
import java.util.List;

public class ColorPalette {

  private final int[] palette;
  private final byte[] colorMap;
  private final int[] fullColorMap;

  public ColorPalette(final List<Integer> colors) {
    this.palette = new int[colors.size()];
    this.colorMap = new byte[128 * 128 * 128];
    this.fullColorMap = new int[128 * 128 * 128];
    this.updateIndices(colors);
    this.createLookupTable(this.forkRed());
  }

  private void createLookupTable( final List<LoadRed> tasks) {
    for (int i = 0; i < 128; i++) {
      final byte[] sub = tasks.get(i).join();
      final int ci = i << 14;
      for (int si = 0; si < 16384; si++) {
        this.colorMap[ci + si] = sub[si];
        this.fullColorMap[ci + si] = this.palette[Byte.toUnsignedInt(sub[si])];
      }
    }
  }

  private List<LoadRed> forkRed() {
    final List<LoadRed> tasks = new ArrayList<>(128);
    for (int r = 0; r < 256; r += 2) {
      final LoadRed red = new LoadRed(this.palette, r);
      tasks.add(red);
      red.fork();
    }
    return tasks;
  }

  private void updateIndices( final List<Integer> colors) {
    int index = 0;
    for (final int color : colors) {
      this.palette[index++] = color;
    }
    this.palette[0] = 0;
  }

  public int[] getPalette() {
    return this.palette;
  }

  public byte[] getColorMap() {
    return this.colorMap;
  }

  public int[] getFullColorMap() {
    return this.fullColorMap;
  }

  /** Init. */
  public static void init() {}
}
