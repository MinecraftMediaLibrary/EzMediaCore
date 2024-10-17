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

public interface Resolution extends Dimension {

  static Resolution ofResolution(final int width, final int height) {
    return new ImmutableResolution(width, height);
  }

  Resolution X360_640 = ofResolution(360, 640);
  Resolution X375_667 = ofResolution(375, 667);
  Resolution X414_896 = ofResolution(414, 896);
  Resolution X360_780 = ofResolution(360, 780);
  Resolution X375_812 = ofResolution(375, 812);

  Resolution X1366_768 = ofResolution(1366, 768);
  Resolution X1920_1080 = ofResolution(1920, 1080);
  Resolution X1536_864 = ofResolution(1536, 864);
  Resolution X1440_900 = ofResolution(1440, 900);
  Resolution X1280_720 = ofResolution(1280, 720);
  Resolution X3840_2160 = ofResolution(3840, 2160);
}
