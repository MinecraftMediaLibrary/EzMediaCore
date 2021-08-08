package io.github.pulsebeat02.ezmediacore.vlc.os.window;

import com.sun.jna.NativeLibrary;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.utility.ArchiveUtils;
import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import io.github.pulsebeat02.ezmediacore.vlc.os.SilentInstallation;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

public class SilentWindowsInstallation extends SilentInstallation {

  public SilentWindowsInstallation(
      @NotNull final MediaLibraryCore core, @NotNull final Path directory) {
    super(core, directory);
  }

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.WINDOWS;
  }

  @Override
  public void downloadBinaries() throws IOException {

    Logger.info("No VLC binary found on machine, installing Windows binaries.");

    final Path folder = this.getDirectory();
    final Path archive = folder.resolve("VLC.zip");

    FileUtils.copyURLToFile(this.core().getDiagnostics().getVlcUrl(), archive);
    Logger.info("Successfully downloaded archived binaries.");

    ArchiveUtils.decompressArchive(archive, folder);
    Logger.info("Successfully extracted archived binaries.");

    this.setInstallationPath(folder.resolve("vlc-3.0.12"));
    this.deleteArchive(archive);
    this.loadNativeBinaries();
  }

  @Override
  public void loadNativeBinaries() throws IOException {
    NativeLibrary.addSearchPath(
        RuntimeUtil.getLibVlcLibraryName(), this.getInstallationPath().toString());
    super.loadNativeBinaries();
  }
}
