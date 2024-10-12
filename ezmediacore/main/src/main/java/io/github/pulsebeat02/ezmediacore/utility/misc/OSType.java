/*
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
package io.github.pulsebeat02.ezmediacore.utility.misc;

import java.util.Locale;
import java.util.stream.Stream;

public enum OSType {
  MAC,
  WINDOWS,
  UNIX;

  private static final String OS;
  private static final String OS_VERSION;
  private static final String OS_ARCH;
  private static final OSType CURRENT_OS;
  private static final boolean IS_UNIX;
  private static final boolean IS_WINDOWS;

  static {
    OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.ROOT);
    OS_ARCH = System.getProperty("os.arch");
    IS_UNIX = Stream.of("nix", "nux", "aix").anyMatch(OS::contains);
    IS_WINDOWS = OS.contains("win");
    CURRENT_OS = IS_UNIX ? UNIX : IS_WINDOWS ? WINDOWS : MAC;
  }

  public static boolean isCurrentOSUnix() {
    return IS_UNIX;
  }

  public static boolean isCurrentOSWin() {
    return IS_WINDOWS;
  }

  public static  OSType getCurrentOS() {
    return CURRENT_OS;
  }

  public static  String getNativeOSValue() {
    return OS;
  }

  public static  String getNativeOSVersionValue() {
    return OS_VERSION;
  }

  public static  String getNativeArchValue() {
    return OS_ARCH;
  }

  public static boolean is64Bit() {
    final boolean isWindows = isCurrentOSWin();
    final boolean envExists = System.getenv("ProgramFiles(x86)") != null;
    final boolean is64bit = OS_ARCH.contains("64");
    return isWindows ? envExists : is64bit;
  }
}
