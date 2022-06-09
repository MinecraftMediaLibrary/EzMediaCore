package io.github.pulsebeat02.ezmediacore.utility.concurrency;

@FunctionalInterface
public interface ThrowingRunnable extends Runnable {

  @Override
  default void run() {
    try {
      this.tryRun();
    } catch (final Throwable t) {
      throwUnchecked(t);
    }
  }

  @SuppressWarnings("unchecked")
  private static <E extends RuntimeException> void throwUnchecked(final Throwable t) {
    throw (E) t;
  }

  void tryRun() throws Throwable;
}
