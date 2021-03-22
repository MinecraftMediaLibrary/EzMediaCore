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
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MacSilentInstallation extends SilentOSDependentSolution {

  public MacSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  public MacSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public boolean checkVLCExistance(@NotNull final String dir) {
    final File folder = findVLCFolder(new File(dir));
    if (folder == null || !folder.exists()) {
      return false;
    }
    loadNativeDependency(folder);
    return getNativeDiscovery().discover();
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(dir)) {
      Logger.info("Found VLC Library in Mac! No need to install into path.");
    } else {
      final File dmg = new File(dir, "VLC.dmg");
      final File diskPath = new File("/Volumes/VLC media player");
      FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), dmg);
      try {
        if (mountDiskImage(dmg) != 0) {
          throw new IOException("Could not Mount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      FileUtils.copyDirectory(new File(diskPath, "VLC.app"), new File(diskPath, "/Applications/VLC.app"));
      Logger.info("Moved File!");
      try {
        if (unmountDiskImage(diskPath.getAbsolutePath()) != 0) {
          throw new IOException("Could not Unmount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      Logger.info("Unmounting Disk Successfully");
      deleteArchive(dmg);
      Logger.info("Deleted DMG File");
    }
  }

  /**
   * Mounts disk image from file.
   *
   * @param dmg disk image
   * @return result code
   * @throws IOException if dmg cannot be found
   * @throws InterruptedException waiting for process
   */
  public int mountDiskImage(@NotNull final File dmg) throws IOException, InterruptedException {
    final String[] command = {"/usr/bin/hdiutil", "attach", dmg.getAbsolutePath()};
    final StringBuilder sendback = new StringBuilder();
    final Process proc = Runtime.getRuntime().exec(command);
    final BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String str;
    while ((str = br.readLine()) != null) {
      sendback.append(str);
    }
    br.close();
    Logger.info("============= DMG INFORMATION =============");
    Logger.info(sendback.toString());
    Logger.info("===========================================");
    return proc.waitFor();
  }

  /**
   * Unmounts disk image from file.
   *
   * @param path disk path
   * @return result code
   * @throws IOException if path cannot be found
   * @throws InterruptedException waiting for process
   */
  public int unmountDiskImage(@NotNull final String path) throws IOException, InterruptedException {
    final String[] command = {"diskutil", "unmount", path};
    final StringBuilder sendback = new StringBuilder();
    final Process proc = Runtime.getRuntime().exec(command);
    final BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    String str;
    while ((str = br.readLine()) != null) {
      sendback.append(str);
    }
    br.close();
    Logger.info("=========== UNMOUNT INFORMATION ===========");
    Logger.info(sendback.toString());
    Logger.info("===========================================");
    return proc.waitFor();
  }

  @Override
  public void loadNativeDependency(@NotNull final File folder) {
    getNativeDiscovery().discover();
  }
}
