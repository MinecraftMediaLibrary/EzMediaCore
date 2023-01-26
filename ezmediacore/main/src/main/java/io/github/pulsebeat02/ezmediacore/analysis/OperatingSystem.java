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
package io.github.pulsebeat02.ezmediacore.analysis;

import io.github.pulsebeat02.ezmediacore.utility.os.OSUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

public final class OperatingSystem implements OperatingSystemInfo {

  private static final Set<String> UNIX;

  static {
    UNIX = Set.of("nix", "nux", "aix");
  }

  private final String osName;
  private final String os;
  private final OSType type;
  private final String version;
  private final String linuxDistro;

  public OperatingSystem() {
    final String name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    final String version = System.getProperty("os.version").toLowerCase(Locale.ROOT);
    this.os = name;
    this.osName = name;
    this.type = this.isWin() ? OSType.WINDOWS : this.isLinux() ? OSType.UNIX : OSType.MAC;
    this.version = version;
    this.linuxDistro = this.getLinuxDistributionCmd();
  }

  private boolean isWin() {
    return this.os.contains("win");
  }

  private boolean isLinux() {
    return UNIX.stream().anyMatch(this.os::contains);
  }

  private @NotNull String getLinuxDistributionCmd() {
    return this.type == OSType.UNIX ? OSUtils.getLinuxDistribution() : "Not Linux!";
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
    return "{os=%s,type=%s,linux-distro=%s}".formatted(this.osName, this.type, this.linuxDistro);
  }
}
