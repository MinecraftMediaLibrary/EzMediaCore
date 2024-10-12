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

public interface Resolution {

  Dimension X360_640 = ofDimension(360, 640);
  Dimension X375_667 = ofDimension(375, 667);
  Dimension X414_896 = ofDimension(414, 896);
  Dimension X360_780 = ofDimension(360, 780);
  Dimension X375_812 = ofDimension(375, 812);

  Dimension X1366_768 = ofDimension(1366, 768);
  Dimension X1920_1080 = ofDimension(1920, 1080);
  Dimension X1536_864 = ofDimension(1536, 864);
  Dimension X1440_900 = ofDimension(1440, 900);
  Dimension X1280_720 = ofDimension(1280, 720);
  Dimension X3840_2160 = ofDimension(3840, 2160);
}
