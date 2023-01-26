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
package io.github.pulsebeat02.ezmediacore.utility.tuple;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Triple<X, Y, Z> {

  private final X x;
  private final Y y;
  private final Z z;

  Triple(@Nullable final X x, @Nullable final Y y, @Nullable final Z z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Contract(value = "_, _, _ -> new", pure = true)
  public static <X, Y, Z> @NotNull Triple<X, Y, Z> ofTriple(
      @Nullable final X x, @Nullable final Y y, @Nullable final Z z) {
    return new Triple<>(x, y, z);
  }

  public @Nullable X getX() {
    return this.x;
  }

  public @Nullable Y getY() {
    return this.y;
  }

  public @Nullable final Z getZ() {
    return this.z;
  }
}
