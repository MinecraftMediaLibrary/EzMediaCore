package rewrite.util.concurrency;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExecutorUtils {

  private ExecutorUtils() {
    throw new UnsupportedOperationException();
  }

  public static boolean shutdownExecutorGracefully(final ExecutorService service) {
    service.shutdown();
    try {
      final boolean await = service.awaitTermination(5, TimeUnit.SECONDS);
      if (!await) {
        final List<Runnable> tasks = service.shutdownNow();
        final String msg = createExecutorShutdownErrorMessage(tasks);
        throw new AssertionError(msg);
      }
    } catch (final InterruptedException e) {
      final Thread current = Thread.currentThread();
      current.interrupt(); // yeah... we're fucked
    }
    return false;
  }

  private static String createExecutorShutdownErrorMessage(final List<Runnable> tasks) {
    final int count = tasks.size();
    return "%s tasks uncompleted!".formatted(count);
  }
}
