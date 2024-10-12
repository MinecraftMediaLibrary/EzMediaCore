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
package io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.platform.osx;

import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.ManualInstallationStrategy;
import io.github.pulsebeat02.ezmediacore.emcinstallers.implementation.vlc.installation.VLCInstaller;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class OSXInstallationStrategy extends ManualInstallationStrategy {

  public OSXInstallationStrategy(final VLCInstaller installer) {
    super(installer);
  }

  @Override
  public Optional<Path> getInstalledPath() {
    final Path path = this.getInstaller().getPath().getParent().resolve("VLC.app");
    return Files.exists(path) ? Optional.of(path) : Optional.empty();
  }

  @Override
  public Path execute() throws IOException {
    final String vlc = "VLC.app";
    final Path dmg = this.getInstaller().getPath();
    final Path disk = Paths.get("/Volumes/VLC media player");
    final Path app = this.getApplicationFolder().resolve(vlc);
    this.mountDisk(dmg);
    this.copyApplication(disk.resolve(vlc), app);
    this.changePermissions(app);
    this.unmountDisk(disk);
    this.deleteFile(dmg);
    return app.resolve("Contents").resolve("MacOS").resolve("lib");
  }

  private Path getApplicationFolder() {
    return Paths.get("/Applications");
  }

  private void unmountDisk(final Path disk) throws IOException {
    this.runNativeProcess("diskutil", "unmount", disk.toString());
  }

  private void changePermissions(final Path app) throws IOException {
    this.runNativeProcess("chmod", "-R", "755", app.toString());
  }

  private void mountDisk(final Path dmg) throws IOException {
    this.runNativeProcess("/usr/bin/hdiutil", "attach", dmg.toString());
  }

  private void copyApplication(final Path src, final Path app) throws IOException {
    this.runNativeProcess("rsync", "-a", String.format("%s/", src.toString()), app.toString());
  }

  private void runNativeProcess(final String... arguments) throws IOException {
    try {
      new ProcessBuilder(arguments).start().waitFor();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }
}
