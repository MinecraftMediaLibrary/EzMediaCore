package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import org.jetbrains.annotations.NotNull;

/** A set of utilities which allow the better debugging output of the media library. */
public final class DebuggerUtilities {

  private DebuggerUtilities() {}

  /**
   * Gets the debug information about a plugin using the library.
   *
   * @param library the library
   * @return the debug string
   */
  public static String getPluginDebugInfo(@NotNull final MinecraftMediaLibrary library) {
    return String.join(
        System.lineSeparator(),
        String.format("Plugin %s initialized MinecraftMediaLibrary", library.getPlugin().getName()),
        String.format("Http Server Path: %s", library.getHttpParentFolder()),
        String.format("Using VLCJ? %s", library.isVlcj() ? "Yes" : "No"));
  }

  /**
   * Gets the debug information about the system operating system using the library.
   *
   * @param library the library
   * @return the debug string
   */
  public static String getSystemDebugInfo(@NotNull final MinecraftMediaLibrary library) {
    return String.join(
        System.lineSeparator(),
        "===========================================",
        "            SYSTEM INFORMATION             ",
        "===========================================",
        String.format("System Operating System: %s", RuntimeUtilities.getOperatingSystem()),
        String.format("CPU Architecture: %s", RuntimeUtilities.getCpuArch()),
        String.format("System Operating System Version: %s", System.getProperty("os.version")),
        String.format(
            "Windows/Mac/Linux: %s/%s/%s",
            RuntimeUtilities.isWindows(), RuntimeUtilities.isMac(), RuntimeUtilities.isLinux()),
        String.format(
            "Linux Distribution (If Linux): %s", RuntimeUtilities.getLinuxDistribution()));
  }
}
