package io.github.pulsebeat02.ezmediacore.utility.concurrency;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ThrowingRunnable extends Runnable {

  @SuppressWarnings("unchecked")
  private static <E extends RuntimeException> void throwUnchecked(@NotNull final Throwable t) {
    t.printStackTrace();
    throw (E) t;
  }

  @Override
  default void run() {
    try {
      this.tryRun();
    } catch (final Throwable throwable) {
      throwUnchecked(throwable);
    }
  }

  void tryRun() throws Throwable;
}
