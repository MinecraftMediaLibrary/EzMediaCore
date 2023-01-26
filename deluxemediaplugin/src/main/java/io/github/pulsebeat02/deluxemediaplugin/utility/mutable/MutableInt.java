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
package io.github.pulsebeat02.deluxemediaplugin.utility.mutable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MutableInt {

  private int number;

  MutableInt(final int number) {
    this.number = number;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofNumber(@NotNull final Number number) {
    return ofInteger(number.intValue());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofInteger(final int number) {
    return new MutableInt(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofString(@NotNull final String string) {
    return ofInteger(Integer.parseInt(string));
  }

  public void increment() {
    this.number++;
  }

  public void decrement() {
    this.number--;
  }

  public void add(final int add) {
    this.number += add;
  }

  public void subtract(final int subtract) {
    this.number -= subtract;
  }

  public void set(final int newNumber) {
    this.number = newNumber;
  }

  public int getNumber() {
    return this.number;
  }
}
