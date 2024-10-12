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
package rewrite.dimension;

import static rewrite.dimension.Dimension.ofDimension;
import static rewrite.dimension.Dimension.square;

public interface ChatWidth {

  Dimension X1_1 = square(1);
  Dimension X2_8 = ofDimension(2, 8);
  Dimension X4_16 = ofDimension(4, 16);
  Dimension X8_16 = ofDimension(8, 16);
  Dimension X8_32 = ofDimension(8, 32);

  Dimension X12_48 = ofDimension(12, 48);
  Dimension X16_48 = ofDimension(16, 48);
  Dimension X16_64 = ofDimension(16, 64);
  Dimension X16_80 = ofDimension(16, 80);
  Dimension X16_92 = ofDimension(16, 92);
}
