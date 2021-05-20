package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.nativestreams.NativeStreams;
import ws.schild.jave.ConversionOutputAnalyzer;

/** A set of utilities which allow the better debugging output of the media library. */
public final class DebuggerUtilities {

  private DebuggerUtilities() {}

  /**
   * Gets the debug information about a plugin using the library.
   *
   * @param library the library
   * @return the debug string
   */
  public static String getPluginDebugInfo(@NotNull final MediaLibrary library) {
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
  public static String getSystemDebugInfo(@NotNull final MediaLibrary library) {
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

  /** Redirects the JAVE2 Logging Output and VLCJ Logging Output to the proper logger file. */
  public static void redirectLoggingOutput() {
    final String path = Logger.getLogFile().getAbsolutePath();
    new NativeStreams(path, path);
    Configurator.setLevel(ConversionOutputAnalyzer.class.getName(), Level.OFF);
  }
}
