package io.github.pulsebeat02.ezmediacore.sneaky;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

  static <T> Consumer<T> unchecked(@NotNull final ThrowingConsumer<? super T, ?> consumer) {
    return t -> {
      try {
        consumer.accept(t);
      } catch (final Throwable e) {
        throw new RuntimeException(e);
      }
    };
  }

  static <T> Consumer<T> sneaky(@NotNull final ThrowingConsumer<? super T, ?> consumer) {
    return t -> {
      try {
        consumer.accept(t);
      } catch (final Exception e) {
        SneakyThrowUtil.sneakyThrow(e);
      }
    };
  }

  void accept(final T t) throws E;
}
