package io.github.pulsebeat02.epicmedialib.vlc.os.window;

import com.sun.jna.NativeLibrary;
import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import io.github.pulsebeat02.epicmedialib.analysis.OSType;
import io.github.pulsebeat02.epicmedialib.utility.ArchiveUtils;
import io.github.pulsebeat02.epicmedialib.utility.FileUtils;
import io.github.pulsebeat02.epicmedialib.vlc.os.SilentInstallation;
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

    final Path folder = getDirectory();
    final Path archive = folder.resolve("VLC.zip");

    FileUtils.copyURLToFile(getCore().getDiagnostics().getVlcUrl(), archive);
    Logger.info("Successfully downloaded archived binaries.");

    ArchiveUtils.decompressArchive(archive, folder);
    Logger.info("Successfully extracted archived binaries.");

    setInstallationPath(folder.resolve("vlc-3.0.12"));
    deleteArchive(archive);
    loadNativeBinaries();
  }

  @Override
  public void loadNativeBinaries() throws IOException {
    NativeLibrary.addSearchPath(
        RuntimeUtil.getLibVlcLibraryName(), getInstallationPath().toString());
    super.loadNativeBinaries();
  }
}
