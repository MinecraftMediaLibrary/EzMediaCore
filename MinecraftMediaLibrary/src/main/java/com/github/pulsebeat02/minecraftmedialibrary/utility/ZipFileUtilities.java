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

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.IOException;

public class ZipFileUtilities {

  public static void decompressArchive(@NotNull final File file, @NotNull final File result) {
    final String extension = FilenameUtils.getExtension(file.getName());
    final Archiver archiver = ArchiverFactory.createArchiver(extension);
    try {
      archiver.extract(file, result);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void decompressArchive(
      @NotNull final File file,
      @NotNull final File result,
      @NotNull final String type,
      @NotNull final String compression) {
    final Archiver archiver = ArchiverFactory.createArchiver(type, compression);
    try {
      archiver.extract(file, result);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
