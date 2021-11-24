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

import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.ofDimension;
import static io.github.pulsebeat02.ezmediacore.dimension.Dimension.square;

public final class FrameDimension {

  public static final Dimension X1_1;
  public static final Dimension X1_2;
  public static final Dimension X3_3;
  public static final Dimension X3_5;
  public static final Dimension X5_5;
  public static final Dimension X6_10;

  public static final Dimension X8_14;
  public static final Dimension X8_18;
  public static final Dimension X10_14;

  static {
    X1_1 = square(1);
    X1_2 = ofDimension(1, 2);
    X3_3 = square(3);
    X3_5 = ofDimension(3, 5);
    X5_5 = square(5);
    X6_10 = ofDimension(6, 10);

    X8_14 = ofDimension(8, 14);
    X8_18 = ofDimension(8, 18);
    X10_14 = ofDimension(10, 14);
  }

  private FrameDimension() {}
}
