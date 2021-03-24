/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Special file utilities used throughout the library and also open to users. Used for easier file
 * management.
 */
public final class FileUtilities {

  /**
   * Download image file from URL.
   *
   * @param url the url
   * @param path the path
   * @return the file
   */
  public static File downloadImageFile(@NotNull final String url, @NotNull final String path) {
    final String filePath = path + "/" + UUID.randomUUID() + ".png";
    try (final InputStream in = new URL(url).openStream()) {
      Files.copy(in, Paths.get(filePath));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return new File(filePath);
  }
}
