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
package io.github.pulsebeat02.ezmediacore.sneaky;

import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  static <T, R> Function<T, Optional<R>> lifted(
      @NotNull final ThrowingFunction<? super T, ? extends R, ?> function) {
    return t -> {
      try {
        return Optional.ofNullable(function.apply(t));
      } catch (final Exception e) {
        return Optional.empty();
      }
    };
  }

  static <T, R> Function<T, R> unchecked(
      @NotNull final ThrowingFunction<? super T, ? extends R, ?> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  static <T1, R> Function<T1, R> sneaky(
      @NotNull final ThrowingFunction<? super T1, ? extends R, ?> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (final Exception ex) {
        return SneakyThrowUtil.sneakyThrow(ex);
      }
    };
  }

  R apply(T arg) throws E;

  default Function<T, Optional<R>> lift() {
    return t -> {
      try {
        return Optional.ofNullable(apply(t));
      } catch (final Exception e) {
        return Optional.empty();
      }
    };
  }
}
