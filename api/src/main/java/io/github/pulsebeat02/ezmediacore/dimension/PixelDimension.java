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

public final class PixelDimension {

  public static final Dimension X360_640;
  public static final Dimension X375_667;
  public static final Dimension X414_896;
  public static final Dimension X360_780;
  public static final Dimension X375_812;

  public static final Dimension X1366_768;
  public static final Dimension X1920_1080;
  public static final Dimension X1536_864;
  public static final Dimension X1440_900;
  public static final Dimension X1280_720;
  public static final Dimension X3840_2160;

  static {
    X360_640 = ofDimension(360, 640);
    X375_667 = ofDimension(375, 667);
    X414_896 = ofDimension(414, 896);
    X360_780 = ofDimension(360, 780);
    X375_812 = ofDimension(375, 812);

    X1366_768 = ofDimension(1366, 768);
    X1920_1080 = ofDimension(1920, 1080);
    X1536_864 = ofDimension(1536, 864);
    X1440_900 = ofDimension(1440, 900);
    X1280_720 = ofDimension(1280, 720);
    X3840_2160 = ofDimension(3840, 2160);
  }

  private PixelDimension() {}
}
