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

public class Pair<K, V> {

  private final K key;
  private final V value;

  Pair(@NotNull final K key, @NotNull final V value) {
    checkNotNull(key, "Key cannot be null!");
    checkNotNull(value, "Value cannot be null!");
    this.key = key;
    this.value = value;
  }

  @Contract(value = "_, _ -> new", pure = true)
  public static <K, V> @NotNull Pair<K, V> ofPair(@NotNull final K key, @NotNull final V value) {
    return new Pair<>(key, value);
  }

  public @NotNull K getKey() {
    return this.key;
  }

  public @NotNull V getValue() {
    return this.value;
  }
}
