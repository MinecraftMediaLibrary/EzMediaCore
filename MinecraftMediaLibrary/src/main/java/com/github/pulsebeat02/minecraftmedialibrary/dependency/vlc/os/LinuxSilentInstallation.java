/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageManager;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * The Linux specific installation for VLC is hard in the sense that we must specify the correct
 * packages to be used for each distribution. Our handling class LinuxPackageManager makes this
 * class's job much easier. It creates an instance of the LinuxPackageManager class, gets the needed
 * package, extracts the contents, and then loads the binaries into the runtime.
 */
public class LinuxSilentInstallation extends SilentOSDependentSolution {

  /**
   * Instantiates a new LinuxSilentInstallation.
   *
   * @param library the library.
   */
  public LinuxSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  /**
   * Instantiates a new LinuxSilentInstallation.
   *
   * @param dir the directory.
   */
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
