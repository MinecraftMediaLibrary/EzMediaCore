/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

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

/**
 * Special archive utilities used throughout the library and also open to users. Used for easier
 * archive management.
 */
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
   * @param result the folder
   * @return the file directory
   */
  public static File decompressArchive(@NotNull final File file, @NotNull final File result) {
    final String name = file.getName();
    final String[] types = getCompressedType(name).split(" ");
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
    return new File(result, FilenameUtils.removeExtension(name));
  }

  /**
   * Decompress archive.
   *
   * @param file the file
   * @param result the folder
   * @param type the type
   * @param compression the compression
   * @return the file directory
   */
  public static File decompressArchive(
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
    return new File(result, FilenameUtils.removeExtension(file.getName()));
  }

  /**
   * Decompress archive.
   *
   * @param file the file
   * @param result the folder
   * @param type the type
   *
   * @return the file directory
   */
  public static File decompressArchive(
      @NotNull final File file, @NotNull final File result, @NotNull final String type) {
    final Archiver archiver = ArchiverFactory.createArchiver(type);
    try {
      archiver.extract(file, result);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return new File(result, FilenameUtils.removeExtension(file.getName()));
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
