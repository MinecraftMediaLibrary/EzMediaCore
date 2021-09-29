package io.github.pulsebeat02.deluxemediaplugin.executors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorProvider {

  public static final Executor STREAM_THREAD_EXECUTOR;

  static {
    STREAM_THREAD_EXECUTOR = Executors.newFixedThreadPool(4);
  }
}
