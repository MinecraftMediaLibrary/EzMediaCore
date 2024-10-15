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
package io.github.pulsebeat02.ezmediacore.installers.vlc.installation;

import io.github.pulsebeat02.ezmediacore.task.CommandTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class OSXInstallationStrategy extends ManualInstallationStrategy {

  private static final String VLC_APP = "VLC.app";

  public OSXInstallationStrategy(final VLCInstaller installer) {
    super(installer);
  }

  @Override
  public Optional<Path> getInstalledPath() {
    final VLCInstaller installer = this.getInstaller();
    final Path path = installer.getPath();
    final Path parent = path.getParent();
    final Path app = parent.resolve("VLC.app");
    return Files.exists(path) ? Optional.of(path) : Optional.empty();
  }

  @Override
  public Path execute() throws IOException {

    final VLCInstaller installer = this.getInstaller();
    final Path disk = Path.of("/Volumes/VLC media player");
    final String raw = disk.toString();

    final Path appFolder = Path.of("/Applications");
    final Path app = appFolder.resolve(VLC_APP);
    final String appRaw = app.toString();

    final Path dmg = installer.getPath();
    final String dmgRaw = dmg.toString();

    final Path src = disk.resolve(VLC_APP);
    final String srcRaw = src.toString() + "/";

    this.runNativeProcess("/usr/bin/hdiutil", "attach", dmgRaw);
    this.runNativeProcess("rsync", "-a", srcRaw, appRaw);
    this.runNativeProcess("chmod", "-R", "755", appRaw);
    this.runNativeProcess("diskutil", "unmount", raw);
    this.deleteFile(dmg);

    final Path contents = app.resolve("Contents");
    final Path macos = contents.resolve("MacOS");
    return macos.resolve("lib");
  }


  private void runNativeProcess(final String... arguments) throws IOException {
    final CommandTask task = new CommandTask(arguments);
    task.run();
  }
}
