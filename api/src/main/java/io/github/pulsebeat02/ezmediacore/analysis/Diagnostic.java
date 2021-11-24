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

import org.jetbrains.annotations.NotNull;

/**
 * This class primarily contains all Diagnostic information including the proper Operating System
 * Information used to download the correct dependencies, sound drivers installed, CPU information,
 * and URLs that point to the correct download locations for the current server hardware.
 */
public interface Diagnostic {

  /**
   * Debugs the information into the Logger class the library uses to debug information for clients.
   */
  void debugInformation();

  /**
   * Gets the information surrounding the Operating System.
   *
   * @return the operating system information
   */
  @NotNull
  OperatingSystemInfo getSystem();

  /**
   * Gets the information surrounding the CPU.
   *
   * @return the CPU information
   */
  @NotNull
  CpuInfo getCpu();
}
