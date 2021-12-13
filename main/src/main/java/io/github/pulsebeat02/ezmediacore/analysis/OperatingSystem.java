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

import io.github.pulsebeat02.ezmediacore.task.CommandTask;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class OperatingSystem implements OperatingSystemInfo {

  private final String osName;
  private final String os;
  private final OSType type;
  private final String version;
  private final String linuxDistro;

  public OperatingSystem() {
    this.os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    this.osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    this.type = this.isWin() ? OSType.WINDOWS : this.isLinux() ? OSType.UNIX : OSType.MAC;
    this.version = System.getProperty("os.version").toLowerCase(Locale.ROOT);
    this.linuxDistro = this.getLinuxDistributionCmd();
  }

  private boolean isWin() {
    return this.os.contains("win");
  }

  private boolean isLinux() {
    return Stream.of("nix", "nux", "aix").anyMatch(this.os::contains);
  }

  private @NotNull String getLinuxDistributionCmd() {
    return this.type == OSType.UNIX ? this.retrieveLinuxDistribution() : "Not Linux!";
  }

  private @NotNull String retrieveLinuxDistribution() {
    try {
      return this.getProcessOutput();
    } catch (final IOException e) {
      e.printStackTrace();
      return "Unknown Distribution";
    }
  }

  private @NotNull String getProcessOutput() throws IOException {
    final CommandTask task = new CommandTask("/bin/sh", "-c", "cat /etc/*-release");
    task.run();
    return task.getOutput();
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
