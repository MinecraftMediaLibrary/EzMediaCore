package io.github.pulsebeat02.ezmediacore.utility.graphics;

import com.sun.jna.platform.DesktopWindow;
import java.util.List;
import java.util.stream.Collectors;

public final class WindowUtils {

  private WindowUtils() {}

  public static List<JNAWindow> getAllWindows() {
    final List<DesktopWindow> windows = com.sun.jna.platform.WindowUtils.getAllWindows(true);
    return windows.stream().map(JNAWindow::new).collect(Collectors.toList());
  }
}
