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
package io.github.pulsebeat02.ezmediacore.dimension;

import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.of;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.square;

public final class ChatDimension {

  public static final Dimension X1_1;
  public static final Dimension X2_8;
  public static final Dimension X4_16;
  public static final Dimension X8_16;
  public static final Dimension X8_32;

  public static final Dimension X12_48;
  public static final Dimension X16_48;
  public static final Dimension X16_64;
  public static final Dimension X16_80;
  public static final Dimension X16_92;

  static {
    X1_1 = square(1);
    X2_8 = of(2, 8);
    X4_16 = of(4, 16);
    X8_16 = of(8, 16);
    X8_32 = of(8, 32);

    X12_48 = of(12, 48);
    X16_48 = of(16, 48);
    X16_64 = of(16, 64);
    X16_80 = of(16, 80);
    X16_92 = of(16, 92);
  }

  private ChatDimension() {}
}
