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
package io.github.pulsebeat02.ezmediacore.dither.load;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;


final class LoadRed extends RecursiveTask<byte[]> {

  @Serial private static final long serialVersionUID = -6408377810782246185L;

  private final int r;
  private final int[] palette;

  LoadRed(final int  [] palette, final int r) {
    this.r = r;
    this.palette = palette;
  }

  @Override
  protected byte  [] compute() {
    final List<LoadGreen> greenSub = new ArrayList<>(128);
    this.forkGreen(greenSub);
    return this.copyColors(greenSub);
  }

  private void forkGreen( final List<LoadGreen> greenSub) {
    for (int g = 0; g < 256; g += 2) {
      final LoadGreen green = new LoadGreen(this.palette, this.r, g);
      greenSub.add(green);
      green.fork();
    }
  }

  private byte  [] copyColors( final List<LoadGreen> greenSub) {
    final byte[] values = new byte[16384];
    for (int i = 0; i < 128; i++) {
      final byte[] sub = greenSub.get(i).join();
      final int index = i << 7;
      System.arraycopy(sub, 0, values, index, 128);
    }
    return values;
  }
}
