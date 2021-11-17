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

package io.github.pulsebeat02.ezmediacore.utility.collection;

import io.github.pulsebeat02.ezmediacore.sneaky.ThrowingConsumer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ArrayUtils {

  private ArrayUtils() {
  }

  public static <T> T @NotNull [] trim(
      final T @NotNull [] array, final int startInclusive, final int endExclusive) {
    return Arrays.copyOfRange(array, startInclusive, endExclusive);
  }

  public static <T> void parallel(final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(consumer);
  }

  public static <T> void parallelSneaky(
      final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(ThrowingConsumer.sneakyConsumer(consumer));
  }

  public static <T> void parallelUnchecked(
      final T @NotNull [] array, @NotNull final Consumer<T> consumer) {
    stream(array).parallel().forEach(ThrowingConsumer.uncheckedConsumer(consumer));
  }

  public static <T> @NotNull List<T> list(final T @NotNull [] array) {
    return stream(array).toList();
  }

  public static <T> @NotNull Set<T> set(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toUnmodifiableSet());
  }

  public static <T> @NotNull Set<T> mutableSet(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toSet());
  }

  public static <T> @NotNull List<T> mutableList(final T @NotNull [] array) {
    return stream(array).collect(Collectors.toList());
  }

  @Contract(pure = true)
  private static <T> @NotNull Stream<T> stream(final T @NotNull [] array) {
    return Arrays.stream(array);
  }
}
