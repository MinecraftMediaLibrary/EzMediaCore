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

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class ArchiveUtilities {

  private static final Set<String> ARCHIVE_EXTENSIONS;

  static {
    ARCHIVE_EXTENSIONS =
        ImmutableSet.of("zip", "deb", "rpm", "txz", "xz", "tgz", "gz", "ar", "cpio");
  }

  /**
   * Decompress archive.
   *
   * @param file the file
   * @param result the result
   */
  public static void decompressArchive(@NotNull final File file, @NotNull final File result) {
    final String[] types = getCompressedType(file.getName()).split(" ");
    final Archiver archiver;
    if (types.length == 1) {
      archiver = ArchiverFactory.createArchiver(types[0]);
    } else {
      archiver = ArchiverFactory.createArchiver(types[0], types[1]);
    }
    try {
      archiver.extract(file, result);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Decompress archive.
   *
   * @param file the file
   * @param result the result
   * @param type the type
   * @param compression the compression
   */
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

  /**
   * Decompress archive.
   *
   * @param file the file
   * @param result the result
   * @param type the type
   */
  public static void decompressArchive(
      @NotNull final File file, @NotNull final File result, @NotNull final String type) {
    final Archiver archiver = ArchiverFactory.createArchiver(type);
    try {
      archiver.extract(file, result);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Recursively decompresses an archive.
   *
   * @param file archive
   * @param folder where to extract to
   */
  public static void recursiveExtraction(@NotNull final File file, @NotNull final File folder) {
    decompressArchive(file, folder);
    File currentFolder = folder;
    final Queue<File> queue = new LinkedList<>(containsArchiveExtension(currentFolder));
    queue.remove(file);
    while (!queue.isEmpty()) {
      final File current = queue.remove();
      currentFolder =
          new File(currentFolder.getAbsolutePath() + "/" + getFileName(current.getName()));
      decompressArchive(current, currentFolder);
      if (!current.getAbsolutePath().equals(file.getAbsolutePath()) && current.delete()) {
        Logger.info("Deleted Zip: " + current.getName() + " successfully");
      } else {
        Logger.error("Could not delete Zip: " + current.getName() + "!");
      }
      final int before = queue.size();
      queue.addAll(containsArchiveExtension(currentFolder));
      if (queue.size() == before) {
        currentFolder = folder;
      }
    }
  }

  /**
   * Checks if files in folders are archive or not.
   *
   * @param f file to check
   * @return set of arhcives in folder
   */
  public static Set<File> containsArchiveExtension(@NotNull final File f) {
    final Set<File> files = new HashSet<>();
    for (final File child : f.listFiles()) {
      for (final String ext : ARCHIVE_EXTENSIONS) {
        if (child.getName().endsWith(ext)) {
          files.add(child);
        }
      }
    }
    return files;
  }

  /**
   * Gets the compressed type format from file name.
   *
   * @param name file name
   * @return archive format
   */
  public static String getCompressedType(@NotNull final String name) {
    if (name.endsWith("zip")) {
      return "zip";
    } else if (name.endsWith("deb") || name.endsWith("ar")) {
      return "ar";
    } else if (name.endsWith("rpm") || name.endsWith("cpio")) {
      return "cpio";
    } else if (name.endsWith("txz") || name.endsWith(".tar.xz")) {
      return "tar xz";
    } else if (name.endsWith("tgz") || name.endsWith(".tar.gz")) {
      return "tar gz";
    } else if (name.endsWith(".tar.zst") || name.endsWith("eopkg")) {
      Logger.warn(
          "Hello user, please read this error carefully: Your computer seems to be using "
              + "KAOS Linux or Solus Linux. The extract for these Linuxes is either a .tar.zst file or an "
              + ".eopkg file, which is yet not supported by the plugin yet. The archive has been downloaded "
              + "in the /vlc folder, and it is required by you to extract the file in order to get the VLC "
              + "libraries. This is a required step, and VLCJ will not run if you do not perform this step.");
    }
    return "";
  }

  /**
   * Gets the file name without any compression headers/extensions
   *
   * @param full file name
   * @return trimmed name
   */
  public static String getFileName(@NotNull final String full) {
    if (full.endsWith(".tar.gz") || full.endsWith(".tar.xz")) {
      return full.substring(0, full.length() - 7);
    } else {
      return FilenameUtils.removeExtension(full);
    }
  }
}
