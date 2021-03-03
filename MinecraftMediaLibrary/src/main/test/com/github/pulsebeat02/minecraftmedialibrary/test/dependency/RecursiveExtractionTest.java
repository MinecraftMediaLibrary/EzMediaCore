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

package com.github.pulsebeat02.minecraftmedialibrary.test.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.utility.ZipFileUtilities;

import java.io.File;

public class RecursiveExtractionTest {

  public static void main(final String[] args) {
    final String prop = System.getProperty("user.dir");
    final File f = new File(prop + "/extraction/test.deb");
    ZipFileUtilities.recursiveExtraction(
        f, new File(prop + "/extraction"));
  }
}
