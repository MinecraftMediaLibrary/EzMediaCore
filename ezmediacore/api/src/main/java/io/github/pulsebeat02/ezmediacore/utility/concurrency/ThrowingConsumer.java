package io.github.pulsebeat02.ezmediacore.utility.concurrency;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {

  @SuppressWarnings("unchecked")
  private static <E extends RuntimeException> void throwUnchecked(final Throwable t) {
    throw (E) t;
  }

  @Override
  default void accept(@NotNull final T t) {
    try {
      this.tryRun();
    } catch (final Throwable throwable) {
      throwUnchecked(throwable);
    }
  }

  void tryRun() throws Throwable;
}
