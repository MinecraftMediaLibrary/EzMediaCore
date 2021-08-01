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
