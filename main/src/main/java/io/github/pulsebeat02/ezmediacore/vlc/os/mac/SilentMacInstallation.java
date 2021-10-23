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
package io.github.pulsebeat02.ezmediacore.vlc.os.mac;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.task.CommandTask;
import io.github.pulsebeat02.ezmediacore.vlc.VLCBinaryChecksum;
import io.github.pulsebeat02.ezmediacore.vlc.os.SilentInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public class SilentMacInstallation extends SilentInstallation {

  public SilentMacInstallation(
      @NotNull final MediaLibraryCore core, @NotNull final Path directory) {
    super(core, directory);
  }

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.MAC;
  }

  private void downloadChecksum(@NotNull final Path dmg) throws IOException {
    new VLCBinaryChecksum(this.getCore().getDiagnostics().getVlcUrl(), dmg).downloadFile();
    Logger.info("Successfully downloaded DMG file!");
  }

  private void mountDisk(@NotNull final Path dmg) throws IOException, InterruptedException {
    if (this.mountDiskImage(dmg) != 0) {
      throw new RuntimeException("A severe I/O error has occurred. Could not mount disk file!");
    }
    Logger.info("Successfully mounted disk!");
  }

  private void copyDiskFiles(@NotNull final Path disk, @NotNull final Path app) throws IOException {
    org.apache.commons.io.FileUtils.copyDirectory(disk.resolve("VLC.app").toFile(), app.toFile());
    Logger.info("Successfully moved VLC app folder!");
  }

  private void changePermissions(@NotNull final Path app) throws IOException, InterruptedException {
    if (this.changePermissionsTask(app) != 0) {
      throw new RuntimeException(
          "A severe permission error has occurred. Could not change permissions of VLC application!");
    }
    Logger.info("Successfully changed permissions for application!");
  }

  private void unmountDisk(@NotNull final Path disk) throws IOException, InterruptedException {
    if (this.unmountDiskImage(disk) != 0) {
      throw new RuntimeException("A severe I/O error has occurred. Could not unmount disk file!");
    }
    Logger.info("Successfully unmounted disk!");
  }

  @Override
  public void downloadBinaries() throws IOException, InterruptedException {
    final Path directory = this.getDirectory();
    final Path dmg = directory.resolve("VLC.dmg");
    final Path disk = Path.of("/Volumes/VLC media player");
    final Path app = directory.resolve("VLC.app");
    this.downloadChecksum(app);
    this.mountDisk(dmg);
    this.copyDiskFiles(disk, app);
    this.changePermissions(app);
    this.unmountDisk(disk);
    this.setInstallationPath(app);
    this.deleteArchive(dmg);
    this.loadNativeBinaries();
  }

  @Override
  public void loadNativeBinaries() throws IOException {
    super.loadNativeBinaries();
  }

  private int mountDiskImage(@NotNull final Path dmg) throws IOException, InterruptedException {
    final CommandTask t =
        new CommandTask(new String[]{"/usr/bin/hdiutil", "attach", dmg.toString()}, true);
    Logger.info("============= DMG INFORMATION =============");
    Logger.info(t.getOutput());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  private int unmountDiskImage(@NotNull final Path path) throws IOException, InterruptedException {
    final CommandTask t =
        new CommandTask(new String[]{"diskutil", "unmount", path.toString()}, true);
    Logger.info("=========== UNMOUNT INFORMATION ===========");
    Logger.info(t.getOutput());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  private int changePermissionsTask(@NotNull final Path path)
      throws IOException, InterruptedException {
    return new CommandTask(new String[]{"chmod", "-R", "755", path.toString()}, true)
        .getProcess()
        .waitFor();
  }
}
