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
package io.github.pulsebeat02.ezmediacore.dimension;

public interface BlockDimension extends Dimension {

  static BlockDimension ofDimension(final int width, final int height) {
    return new ImmutableBlockDimension(width, height);
  }

  static BlockDimension square(final int side) {
    return new ImmutableBlockDimension(side, side);
  }

  BlockDimension X1_1 = square(1);
  BlockDimension X1_2 = ofDimension(1, 2);
  BlockDimension X3_3 = square(3);
  BlockDimension X3_5 = ofDimension(3, 5);
  BlockDimension X5_5 = square(5);
  BlockDimension X6_10 = ofDimension(6, 10);

  BlockDimension X8_14 = ofDimension(8, 14);
  BlockDimension X8_18 = ofDimension(8, 18);
  BlockDimension X10_14 = ofDimension(10, 14);
}
