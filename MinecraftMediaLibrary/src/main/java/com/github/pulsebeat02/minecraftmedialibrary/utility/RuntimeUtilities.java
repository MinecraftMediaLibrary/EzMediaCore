/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Special runtime utilities used throughout the library and also open to users. Used for easier
 * runtime management.
 */
public final class RuntimeUtilities {

  /** CPU Architecture */
  private static final String CPU_ARCH;

  /** 64 Bit Architecture */
  private static final boolean is64bit;

  /** Operating System */
  private static final String OPERATING_SYSTEM;

  /** Linux Distribution (If using linux) */
  private static final String LINUX_DISTRIBUTION;

  /** Using MAC */
  private static final boolean MAC;

  /** Using WINDOWS */
  private static final boolean WINDOWS;

  /** Using LINUX */
  private static final boolean LINUX;

  /** URL used to download or "LINUX" if Linux */
  private static String URL;

  static {
    Logger.info("Detecting Operating System...");
    OPERATING_SYSTEM = getOperatingSystem().toLowerCase();
    CPU_ARCH = getCpuArchitecture();
    LINUX =
        OPERATING_SYSTEM.contains("nix")
            || OPERATING_SYSTEM.contains("nux")
            || OPERATING_SYSTEM.contains("aix");
    WINDOWS = OPERATING_SYSTEM.contains("win");
    MAC = OPERATING_SYSTEM.contains("mac");
    LINUX_DISTRIBUTION = LINUX ? getLinuxDistribution() : "";
    if (is64Architecture(OPERATING_SYSTEM)) {
      is64bit = true;
      if (WINDOWS) {
        Logger.info("Detected Windows 64 Bit!");
        URL = "http://download.videolan.org/pub/videolan/vlc/last/win64/vlc-3.0.12-win64.zip";
      } else if (LINUX) {
        if (CPU_ARCH.contains("arm")) {
          Logger.info("Detected Linux ARM 64 Bit!");
        } else {
          Logger.info("Detected Linux AMD/Intel 64 Bit!");
        }
        URL = "LINUX";
      } else if (MAC) {
        if (!CPU_ARCH.contains("amd")) {
          Logger.info("Detected MACOS 64 Bit! (Silicon)");
          URL =
              "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-intel64/VLC.dmg";
        } else {
          Logger.info("Detected MACOS 64 Bit! (AMD)");
          URL = "https://github.com/PulseBeat02/VLC-Release-Mirror/raw/master/macos-arm64/VLC.dmg";
        }
      }
    } else {
      is64bit = false;
      if (WINDOWS) {
        URL = "http://download.videolan.org/pub/videolan/vlc/last/win32/vlc-3.0.12-win32.zip";
      } else if (LINUX) {
        if (CPU_ARCH.contains("arm")) {
          Logger.info("Detected ARM 32 Bit!");
          URL = "LINUX";
        }
      }
    }
    Logger.info("=========================================");
    Logger.info(" Final Results After Runtime Scanning... ");
    Logger.info("=========================================");
    Logger.info("Operating System: " + OPERATING_SYSTEM);
    Logger.info("CPU Architecture: " + CPU_ARCH);
    Logger.info("Link Used: " + URL);
    Logger.info("=========================================");
  }

  /**
   * Gets Operating System String.
   *
   * @return the operating system
   */
  public static String getOperatingSystem() {
    return System.getProperty("os.name");
  }

  /**
   * Gets CPU Architecture.
   *
   * @return the cpu architecture
   */
  public static String getCpuArchitecture() {
    return System.getProperty("os.arch");
  }

  /**
   * Gets linux distribution. Returns an empty string if the operating system is not windows.
   *
   * @return the linux distribution
   */
  public static String getLinuxDistribution() {
    if (!LINUX) {
      return "";
    }
    final String[] cmd = {"/bin/sh", "-c", "cat /etc/*-release"};
    final StringBuilder concat = new StringBuilder();
    try {
      final Process p = Runtime.getRuntime().exec(cmd);
      final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = bri.readLine()) != null) {
        concat.append(line);
        concat.append(" ");
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return concat.toString();
  }

  /**
   * Is 64 architecture boolean.
   *
   * @param os the os
   * @return the boolean
   */
  public static boolean is64Architecture(@NotNull final String os) {
    if (os.contains("Windows")) {
      return System.getenv("ProgramFiles(x86)") != null;
    } else {
      return System.getProperty("os.arch").contains("64");
    }
  }

  /**
   * Gets distribution name.
   *
   * @param distro the distro
   * @return the distribution name
   */
  public static String getDistributionName(@NotNull final String distro) {
    final String[] arr = distro.split(" ");
    for (final String str : arr) {
      if (str.startsWith("NAME")) {
        return str.substring(6, str.length() - 1);
      }
    }
    return "";
  }

  /**
   * Gets distribution version.
   *
   * @param distro the distro
   * @return the distribution version
   */
  public static String getDistributionVersion(@NotNull final String distro) {
    final String[] arr = distro.split(" ");
    for (final String str : arr) {
      if (str.startsWith("VERSION")) {
        return str.substring(8, str.length() - 1);
      }
    }
    return "";
  }

  /**
   * Gets CPU Architecture.
   *
   * @return the cpu arch
   */
  public static String getCpuArch() {
    return CPU_ARCH;
  }

  /**
   * Checks if OS is Mac.
   *
   * @return Mac OS
   */
  public static boolean isMac() {
    return MAC;
  }

  /**
   * Checks if OS is Windows.
   *
   * @return Windows OS
   */
  public static boolean isWindows() {
    return WINDOWS;
  }

  /**
   * Checks if OS is Linux.
   *
   * @return Linux OS
   */
  public static boolean isLinux() {
    return LINUX;
  }

  /**
   * Gets VLC url.
   *
   * @return vlc installation url
   */
  public static String getURL() {
    return URL;
  }

  /**
   * Returns whether the operating system is 64 bit or not.
   *
   * @return if the operating system is 64 bit
   */
  public static boolean is64bit() {
    return is64bit;
  }

  /**
   * Executes a script in Linux.
   *
   * @param file the script
   * @param message the success message
   */
  public static void executeBashScript(
      @NotNull final File file, @NotNull final String[] arguments, @NotNull final String message) {
    try {
      final ProcessBuilder pb =
          new ProcessBuilder("bash", file.getAbsolutePath(), String.join(" ", arguments));
      final ProcessBuilder.Redirect redirect = ProcessBuilder.Redirect.appendTo(Logger.getLogFile());
      pb.redirectOutput(redirect);
      pb.redirectError(redirect);
      Logger.info(
          pb.start().waitFor() == 0
              ? message
              : "An issue occurred while running script! (" + file.getAbsolutePath() + ")");
    } catch (final InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }
}
