package io.github.pulsebeat02.ezmediacore.vlc.os.mac;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.task.CommandTask;
import io.github.pulsebeat02.ezmediacore.utility.DependencyUtils;
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

  @Override
  public void downloadBinaries() throws IOException, InterruptedException {

    final Path directory = this.getDirectory();
    final Path dmg = directory.resolve("VLC.dmg");
    final Path disk = Path.of("/Volumes/VLC media player");
    final Path app = directory.resolve("VLC.app");

    DependencyUtils.downloadFile(dmg, this.getCore().getDiagnostics().getVlcUrl());

    if (this.mountDiskImage(dmg) != 0) {
      throw new RuntimeException("A severe I/O error has occurred. Could not mount disk file!");
    }
    Logger.info("Successfully mounted disk!");

    org.apache.commons.io.FileUtils.copyDirectory(disk.resolve("VLC.app").toFile(), app.toFile());

    if (this.changePermissions(app) != 0) {
      throw new RuntimeException(
          "A severe permission error has occurred. Could not change permissions of VLC application!");
    }
    Logger.info("Successfully changed permissions for application!");

    if (this.unmountDiskImage(disk) != 0) {
      throw new RuntimeException("A severe I/O error has occurred. Could not unmount disk file!");
    }
    Logger.info("Successfully unmounted disk!");

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
    Logger.info(t.getResult());
    Logger.info("===========================================");

    return t.getProcess().waitFor();
  }

  private int unmountDiskImage(@NotNull final Path path) throws IOException, InterruptedException {

    final CommandTask t =
        new CommandTask(new String[]{"diskutil", "unmount", path.toString()}, true);

    Logger.info("=========== UNMOUNT INFORMATION ===========");
    Logger.info(t.getResult());
    Logger.info("===========================================");

    return t.getProcess().waitFor();
  }

  private int changePermissions(@NotNull final Path path) throws IOException, InterruptedException {
    return new CommandTask(new String[]{"chmod", "-R", "755", path.toString()}, true)
        .getProcess()
        .waitFor();
  }
}
