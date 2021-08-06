package io.github.pulsebeat02.ezmediacore.utility;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

public final class ThreadUtils {

  private ThreadUtils() {
  }

  public static void createThreadDump() {
    final ThreadInfo[] threads = ManagementFactory.getThreadMXBean()
        .dumpAllThreads(true, true);
    for (final ThreadInfo info : threads) {
      System.out.print(info);
    }
  }

}
