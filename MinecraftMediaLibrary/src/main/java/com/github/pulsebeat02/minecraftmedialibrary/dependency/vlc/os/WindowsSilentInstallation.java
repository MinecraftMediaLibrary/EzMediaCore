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
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.sun.jna.NativeLibrary;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WindowsSilentInstallation extends SilentOSDependentSolution {

  public WindowsSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  public WindowsSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public boolean checkVLCExistance(@NotNull final String dir) {
    final File folder = findVLCFolder(new File(dir));
    if (folder == null || !folder.exists()) {
      return false;
    }
    loadNativeDependency(folder);
    return getEnhancedNativeDiscovery().discover() != null;
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(getDir())) {
      Logger.info("Found VLC Library in Windows! No need to install into path.");
    } else {
      Logger.info("No VLC Installation found on this Computer. Proceeding to a manual install.");
      final File zip = new File(dir, "VLC.zip");
      FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), zip);
      final String path = zip.getAbsolutePath();
      Logger.info("Zip File Path: " + path);
      ArchiveUtilities.decompressArchive(new File(path), new File(dir));
      Logger.info("Successfully Extracted File");
      deleteArchive(zip);
      loadNativeDependency(new File(dir));
      printSystemEnvironmentVariables();
      printSystemProperties();
    }
  }

  @Override
  public void loadNativeDependency(@NotNull final File folder) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), folder.getAbsolutePath());
  }
}
