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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation;

import static io.github.pulsebeat02.emcinstallers.OS.MAC;
import static io.github.pulsebeat02.emcinstallers.OS.WINDOWS;
import static io.github.pulsebeat02.emcinstallers.OS.getOperatingSystem;

import com.google.common.collect.Table;
import io.github.pulsebeat02.emcinstallers.OS;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.BaseInstaller;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.installation.platform.osx.OSXInstallationStrategy;
import io.github.pulsebeat02.emcinstallers.implementation.vlc.installation.platform.win.WinInstallationStrategy;
import io.github.pulsebeat02.emcinstallers.utils.ResourceUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class VLCInstaller extends BaseInstaller {

  public static final String VERSION;
  private static final Table<OS, Boolean, String> BITS_64;
  private static final Table<OS, Boolean, String> BITS_32;

  static {
    VERSION = "3.0.18";
    BITS_64 = ResourceUtils.parseTable("/emcinstallers-json/vlc/bits64.json");
    BITS_32 = ResourceUtils.parseTable("/emcinstallers-json/vlc/bits32.json");
  }

  VLCInstaller(final Path folder) {
    super(folder, "vlc", BITS_32, BITS_64);
  }

  VLCInstaller() {
    super("vlc", BITS_32, BITS_64);
  }

  /**
   * Constructs a new VLCInstaller with the specified directory for the executable.
   *
   * @param executable directory
   * @return new VLCInstaller
   */
  public static VLCInstaller create(final Path executable) {
    return new VLCInstaller(executable);
  }

  /**
   * Constructs a new VLCInstaller with the default directory for the executable.
   * <p>
   * For Windows, it is C:/Program Files/static-emc Otherwise, it is [user home
   * directory]/static-emc
   *
   * @return new VLCInstaller
   */
  public static VLCInstaller create() {
    return new VLCInstaller();
  }

  @Override
  public Path download(final boolean chmod) throws IOException {

    final InstallationStrategy strategy = this.getStrategy();
    final Optional<Path> optional = strategy.getInstalledPath();
    if (optional.isPresent()) {
      return optional.get();
    }

    super.download(chmod);

    return strategy.execute();
  }

  private InstallationStrategy getStrategy() {
    switch (getOperatingSystem()) {
      case MAC:
        return new OSXInstallationStrategy(this);
      case WINDOWS:
        return new WinInstallationStrategy(this);
      default:
        throw new AssertionError("Operating System not Supported!");
    }
  }

  @Override
  public boolean isSupported() {
    final OS os = getOperatingSystem();
    return os == WINDOWS || os == MAC;
  }
}
