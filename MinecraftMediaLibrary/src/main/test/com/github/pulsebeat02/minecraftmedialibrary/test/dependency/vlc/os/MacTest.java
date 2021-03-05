/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/4/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.glytching.junit.extension.system.SystemProperty;
import org.junit.jupiter.api.Test;

import java.io.File;

public class MacTest {

  @Test
  @SystemProperty(name = "os.name", value = "Linux")
  public void linuxTest() {
    Logger.setVerbose(true);
    final File folder = new File(new File(System.getProperty("user.dir")).getParent() + "/vlc");
    if (!folder.exists()) {
      if (folder.mkdir()) {
        System.out.println("Made Folder");
      } else {
        System.out.println("Could NOY Make Folder");
      }
    }
    final VLCNativeDependencyFetcher fetcher =
        new VLCNativeDependencyFetcher(folder.getAbsolutePath());
    fetcher.downloadLibraries();
  }
}
