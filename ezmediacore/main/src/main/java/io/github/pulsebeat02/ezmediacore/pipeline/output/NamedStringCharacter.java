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
package io.github.pulsebeat02.ezmediacore.pipeline.output;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NamedStringCharacter {

  public static final NamedStringCharacter DASH;
  public static final NamedStringCharacter HYPHEN;
  public static final NamedStringCharacter PERIOD;

  public static final NamedStringCharacter TINY_SQUARE;
  public static final NamedStringCharacter NORMAL_SQUARE;
  public static final NamedStringCharacter VERTICAL_RECTANGLE;
  public static final NamedStringCharacter HORIZONTAL_RECTANGLE;

  static {
    DASH = ofString("-");
    HYPHEN = ofString("—");
    PERIOD = ofString(".");

    TINY_SQUARE = ofString("■");
    NORMAL_SQUARE = ofString("■");
    VERTICAL_RECTANGLE = ofString("█");
    HORIZONTAL_RECTANGLE = ofString("▬");
  }

  private final String character;

  NamedStringCharacter( final String name) {
    checkNotNull(name, "NamedEntityString cannot be null!");
    this.character = name;
  }

  public static  NamedStringCharacter ofString( final String name) {
    return new NamedStringCharacter(name);
  }

  public  String getCharacter() {
    return this.character;
  }
}
