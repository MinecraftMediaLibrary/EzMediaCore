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
package rewrite.dither.algorithm;

import rewrite.dither.NativeDitherAlgorithm;
import rewrite.dither.load.ColorPalette;
import rewrite.dither.load.DefaultPalette;
import rewrite.natives.DitherLibC;
import java.util.function.BiFunction;

public abstract class ForeignDitherAlgorithm implements NativeDitherAlgorithm {

  private final BiFunction<int[], Integer, byte[]> function;
  private final ColorPalette palette;

  public ForeignDitherAlgorithm(final ColorPalette palette, final boolean useNative) {
    if (useNative) {
      this.tryUsingNative();
    }
    this.palette = palette;
    this.function = useNative ? this::ditherIntoMinecraftNatively : this::standardMinecraftDither;
  }

  public ForeignDitherAlgorithm() {
    this(new DefaultPalette(), false);
  }

  private void tryUsingNative() {
    if (!DitherLibC.isSupported()) {
      this.throwException();
    }
  }

  private void throwException() {
    throw new UnsupportedOperationException(
        "Your current platform does not support native dithering!");
  }

  public abstract byte[] standardMinecraftDither(int[] buffer, int width);

  @Override
  public byte[] ditherIntoMinecraft(final int  [] buffer, final int width) {
    return this.function.apply(buffer, width);
  }

  @Override
  public ColorPalette getPalette() {
    return this.palette;
  }
}
