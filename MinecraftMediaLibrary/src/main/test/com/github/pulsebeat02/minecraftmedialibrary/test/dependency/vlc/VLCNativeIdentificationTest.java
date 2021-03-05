/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/3/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.EnchancedNativeDiscovery;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.VLCNativeDependencyFetcher;

import java.io.File;

public class VLCNativeIdentificationTest {

  public static void main(final String[] args) {
    final String path = new File(System.getProperty("user.dir") + "/vlc").getAbsolutePath();
    final VLCNativeDependencyFetcher fetcher = new VLCNativeDependencyFetcher(path);
    fetcher.downloadLibraries();
    final EnchancedNativeDiscovery enchancedNativeDiscovery =
        new EnchancedNativeDiscovery(path);
    System.out.println(enchancedNativeDiscovery.discover());
  }
}
