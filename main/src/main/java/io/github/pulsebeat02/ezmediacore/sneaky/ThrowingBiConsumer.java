package io.github.pulsebeat02.ezmediacore.sneaky;

import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingBiConsumer<T1, T2, EX extends Exception> {

  static <T, U> BiConsumer<T, U> unchecked(
      @NotNull final ThrowingBiConsumer<? super T, ? super U, ?> consumer) {
    return (arg1, arg2) -> {
      try {
        consumer.accept(arg1, arg2);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  static <T, U> BiConsumer<T, U> sneaky(
      @NotNull final ThrowingBiConsumer<? super T, ? super U, ?> consumer) {
    return (arg1, arg2) -> {
      try {
        consumer.accept(arg1, arg2);
      } catch (final Exception e) {
        SneakyThrowUtil.sneakyThrow(e);
      }
    };
  }

  void accept(T1 t, T2 t2) throws EX;
}
