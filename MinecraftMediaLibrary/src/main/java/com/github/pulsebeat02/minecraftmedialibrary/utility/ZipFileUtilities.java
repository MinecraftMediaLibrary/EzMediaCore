/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileUtilities {

  public static void unzip(final @NotNull String zipFilePath, final @NotNull String destDirectory) {
    final File destDir = new File(destDirectory);
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    try {
      final ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
      ZipEntry entry = zipIn.getNextEntry();
      while (entry != null) {
        final String filePath = destDirectory + File.separator + entry.getName();
        if (!entry.isDirectory()) {
          extractFile(zipIn, filePath);
        } else {
          File dir = new File(filePath);
          dir.mkdirs();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
      zipIn.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private static void extractFile(@NotNull ZipInputStream zipIn, @NotNull String filePath)
      throws IOException {
    final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    final byte[] bytesIn = new byte[4096];
    int read;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }
}
