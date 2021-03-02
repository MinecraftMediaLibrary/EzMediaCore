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

import com.github.pulsebeat02.minecraftmedialibrary.exception.InvalidYoutubeURLException;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtractorUtilities {

  /**
   * Extracts vidoe id from Youtube URL.
   *
   * @param url the url
   * @return the video id
   */
  public static String getVideoID(@NotNull final String url) {
    final Pattern compiledPattern =
        Pattern.compile("(?<=youtu.be/|watch\\?v=|/videos/|embed)[^#]*");
    final Matcher matcher = compiledPattern.matcher(url);
    if (matcher.find()) {
      final String id = matcher.group();
      Logger.info("Found Video ID for " + url + "(" + id + ")");
      return id;
    }
    throw new InvalidYoutubeURLException("Cannot extract Video ID (" + url + ")");
  }

  /**
   * Create sha1 hash from a file.
   *
   * @param file the file
   * @return the byte [ ]
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws IOException the io exception
   */
  public static byte[] createHashSHA(@NotNull final File file)
      throws NoSuchAlgorithmException, IOException {
    final MessageDigest digest = MessageDigest.getInstance("SHA-1");
    final InputStream fis = new FileInputStream(file);
    int n = 0;
    final byte[] buffer = new byte[8192];
    while (n != -1) {
      n = fis.read(buffer);
      if (n > 0) {
        digest.update(buffer, 0, n);
      }
    }
    final byte[] hash = digest.digest();
    Logger.info(
        "Generated Hash for File " + file.getAbsolutePath() + " (" + new String(hash) + ")");
    return hash;
  }
}
