package io.github.pulsebeat02.epicmedialib.sneaky;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

  static <T, E extends Throwable> Consumer<T> unchecked(
      @NotNull final ThrowingConsumer<T, ?> consumer,
      @NotNull final Logger logger,
      @NotNull final Level level,
      @NotNull final String message) {
    return (t) -> {
      try {
        consumer.accept(t);
      } catch (final Throwable e) {
        logger.log(level, message);
        e.printStackTrace();
      }
    };
  }

  static <T, E extends Throwable> Consumer<T> unchecked(
      @NotNull final ThrowingConsumer<T, ?> consumer, @NotNull final String message) {
    return (t) -> {
      try {
        consumer.accept(t);
      } catch (final Throwable e) {
        io.github.pulsebeat02.epicmedialib.Logger.info(message);
        throw new RuntimeException(e);
      }
    };
  }

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
