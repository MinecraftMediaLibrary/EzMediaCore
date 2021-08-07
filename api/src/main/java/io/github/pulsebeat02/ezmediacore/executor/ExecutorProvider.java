package io.github.pulsebeat02.ezmediacore.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ExecutorProvider {

  public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE;
  public static final ExecutorService HTTP_REQUEST_POOL;
  public static final ExecutorService MAP_UPDATE_POOL;
  public static final ExecutorService EXTERNAL_PROCESS_POOL;

  static {
    SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
    HTTP_REQUEST_POOL = Executors.newCachedThreadPool();
    MAP_UPDATE_POOL = Executors.newCachedThreadPool();
    EXTERNAL_PROCESS_POOL = Executors.newSingleThreadExecutor();
  }

  private ExecutorProvider() {
  }

}
