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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public final class OperatingSystem implements OperatingSystemInfo {

  private final String osName;
  private final OSType type;
  private final String version;
  private final String linuxDistro;

  OperatingSystem() {
    this.osName = OSType.getNativeOSValue();
    this.type = OSType.getCurrentOS();
    this.version = OSType.getNativeOSVersionValue();
    this.linuxDistro = type == OSType.UNIX ? this.retrieveLinuxDistribution() : "";
  }

  private @NotNull String retrieveLinuxDistribution() {
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

  @Override
  public @NotNull String getOSName() {
    return this.osName;
  }

  @Override
  public @NotNull OSType getOSType() {
    return this.type;
  }

  @Override
  public @NotNull String getLinuxDistribution() {
    return this.linuxDistro;
  }

  @Override
  public @NotNull String getVersion() {
    return this.version;
  }

  @Override
  public String toString() {
    return "{os=%s,type=%s,linux-distro=%s}"
        .formatted(this.osName, this.type.name().toLowerCase(Locale.ROOT), this.linuxDistro);
  }
}
