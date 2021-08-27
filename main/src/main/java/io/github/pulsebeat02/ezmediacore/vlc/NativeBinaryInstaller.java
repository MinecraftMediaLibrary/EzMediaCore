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
package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.vlc.os.SilentInstallationProvider;
import io.github.pulsebeat02.ezmediacore.vlc.os.mac.SilentMacInstallation;
import io.github.pulsebeat02.ezmediacore.vlc.os.unix.SilentUnixInstallation;
import io.github.pulsebeat02.ezmediacore.vlc.os.window.SilentWindowsInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class NativeBinaryInstaller implements BinaryInstaller {

  private final MediaLibraryCore core;
  private final SilentInstallationProvider provider;

  public NativeBinaryInstaller(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.provider = this.getInstallation();
  }

  public NativeBinaryInstaller(
      @NotNull final MediaLibraryCore core, @NotNull final SilentInstallationProvider provider) {
    this.core = core;
    this.provider = provider;
  }

  private SilentInstallationProvider getInstallation() {
    final Path path = this.core.getVlcPath();
    return switch (this.core.getDiagnostics().getSystem().getOSType()) {
      case MAC -> new SilentMacInstallation(this.core, path);
      case UNIX -> new SilentUnixInstallation(this.core, path);
      case WINDOWS -> new SilentWindowsInstallation(this.core, path);
    };
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public @NotNull SilentInstallationProvider getProvider() {
    return this.provider;
  }

  @Override
  public void download() throws IOException, InterruptedException {
    this.provider.downloadBinaries();
  }
}
