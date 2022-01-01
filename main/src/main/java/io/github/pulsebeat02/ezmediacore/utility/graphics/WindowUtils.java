package io.github.pulsebeat02.ezmediacore.utility.graphics;

import java.util.List;
import java.util.stream.Collectors;

public final class WindowUtils {

  private WindowUtils() {}

  public static List<JNAWindow> getAllWindows() {
    return com.sun.jna.platform.WindowUtils.getAllWindows(true).stream()
        .map(JNAWindow::new)
        .collect(Collectors.toList());
  }
}
