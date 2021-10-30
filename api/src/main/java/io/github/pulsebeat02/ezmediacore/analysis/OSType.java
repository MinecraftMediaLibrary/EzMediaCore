/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.analysis;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public enum OSType {
  MAC,
  WINDOWS,
  UNIX;

  private static final String OS;
  private static final String OS_VERSION;
  private static final String OS_ARCH;
  private static final OSType CURRENT_OS;

  static {
    OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.ROOT);
    OS_ARCH = System.getProperty("os.arch");
    CURRENT_OS = isUnix() ? UNIX : isWin() ? WINDOWS : MAC;
  }

  private static boolean isUnix() {
    return Stream.of("nix", "nux", "aix").anyMatch(OS::contains);
  }

  private static boolean isWin() {
    return OS.contains("win");
  }

  public static @NotNull OSType getCurrentOS() {
    return CURRENT_OS;
  }

  public static @NotNull String getNativeOSValue() {
    return OS;
  }

  public static @NotNull String getNativeOSVersionValue() {
    return OS_VERSION;
  }

  public static @NotNull String getNativeArchValue() {
    return OS_ARCH;
  }

  public static boolean is64Bit() {
    return CURRENT_OS == WINDOWS
        ? System.getenv("ProgramFiles(x86)") != null
        : OS_ARCH.contains("64");
  }
}
