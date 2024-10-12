/**
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.emcinstallers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public enum OS {
  LINUX,
  FREEBSD,
  MAC,
  WINDOWS;

  private static final String OS_ARCH;
  private static final OS CURRENT;
  private static final boolean BITS_64;
  private static final boolean ARM;
  private static final Path EXECUTABLES;

  static {
    OS_ARCH = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
    CURRENT = getOperatingSystem0();
    BITS_64 = is64Bits0();
    ARM = isArm0();
    EXECUTABLES = getPath0();
  }

  private static OS getOperatingSystem0() {
    final String os = System.getProperty("os.name").toLowerCase();
    return os.contains("win")
        ? WINDOWS
        : os.contains("mac") ? MAC : os.contains("freebsd") ? FREEBSD : LINUX;
  }

  private static boolean is64Bits0() {
    if (CURRENT == WINDOWS) {
      final String arch = System.getenv("PROCESSOR_ARCHITECTURE");
      final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
      return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64");
    } else {
      return OS_ARCH.contains("64");
    }
  }

  private static Path getPath0() {
    return Paths.get(System.getProperty("user.home"), "static-emc");
  }

  private static boolean isArm0() {
    return OS_ARCH.contains("arm");
  }

  public static OS getOperatingSystem() {
    return CURRENT;
  }

  public static boolean isBits64() {
    return BITS_64;
  }

  public static boolean isArm() {
    return ARM;
  }

  public static Path getExecutablePath() {
    return EXECUTABLES;
  }
}
