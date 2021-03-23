package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageManager;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class LinuxSilentInstallation extends SilentOSDependentSolution {

  public LinuxSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  public LinuxSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(dir)) {
      Logger.info("Found VLC Library in Linux! No need to install into path.");
    } else {
      Logger.info("No VLC Installation found on this Computer. Proceeding to a manual install.");
      final LinuxPackageManager manager = new LinuxPackageManager(dir);
      final File f = manager.getPackage();
      manager.extractContents();
      Logger.info("Downloaded and Extracted Package (" + f.getAbsolutePath() + ")");
      deleteArchive(f);
      loadNativeDependency(new File(dir));
      printSystemEnvironmentVariables();
      printSystemProperties();
    }
  }
}
