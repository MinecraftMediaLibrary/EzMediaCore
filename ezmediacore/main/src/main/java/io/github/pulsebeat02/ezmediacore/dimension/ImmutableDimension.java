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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import org.jetbrains.annotations.Contract;

import org.jetbrains.annotations.Unmodifiable;

public final class ImmutableDimension implements Dimension {

  private final int width;
  private final int height;

  public ImmutableDimension(final int width, final int height) {
    checkArgument(width >= 0, "Width must be above or equal to 0!");
    checkArgument(height >= 0, "Height must be above or equal to 0!");
    this.width = width;
    this.height = height;
  }

  @Contract(" -> new")
  @Override
  public  @Unmodifiable Map<String, Object> serialize() {
    return Map.of(
        "width", this.width,
        "height", this.height);
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }
}
