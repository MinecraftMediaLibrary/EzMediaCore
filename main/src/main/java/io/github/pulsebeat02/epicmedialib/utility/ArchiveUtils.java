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

package io.github.pulsebeat02.epicmedialib.utility;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.epicmedialib.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

public final class ArchiveUtils {

  private static final Set<String> ARCHIVE_EXTENSIONS;

  static {
    ARCHIVE_EXTENSIONS =
        ImmutableSet.of("zip", "deb", "rpm", "txz", "xz", "tgz", "gz", "ar", "cpio");
  }

  private ArchiveUtils() {}

  public static void decompressArchive(@NotNull final Path file, @NotNull final Path result) {
    final String name = PathUtils.getName(file);
    final String[] types = getCompressedType(name).split(" ");
    final Archiver archiver;
    if (types.length == 1) {
      archiver = ArchiverFactory.createArchiver(types[0]);
    } else {
      archiver = ArchiverFactory.createArchiver(types[0], types[1]);
    }
    try {
      archiver.extract(file.toFile(), result.toFile());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void decompressArchive(
      @NotNull final Path file,
      @NotNull final Path result,
      @NotNull final String type,
      @NotNull final String compression) {
    final Archiver archiver = ArchiverFactory.createArchiver(type, compression);
    try {
      archiver.extract(file.toFile(), result.toFile());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void decompressArchive(
      @NotNull final Path file, @NotNull final Path result, @NotNull final String type) {
    final Archiver archiver = ArchiverFactory.createArchiver(type);
    try {
      archiver.extract(file.toFile(), result.toFile());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void recursiveExtraction(@NotNull final Path file, @NotNull final Path folder) {
    decompressArchive(file, folder);
    Path currentFolder = folder;
    final Queue<Path> queue = new LinkedList<>(containsArchiveExtension(currentFolder));
    queue.remove(file);
    while (!queue.isEmpty()) {
      final Path current = queue.remove();
      currentFolder =
          Paths.get(
              String.format(
                  "%s/%s",
                  currentFolder.toAbsolutePath(), getFileName(PathUtils.getName(current))));
      decompressArchive(current, currentFolder);
      if (!current.toAbsolutePath().toString().equals(file.toAbsolutePath().toString())) {
        try {
          Files.delete(current);
          Logger.info(String.format("Deleted Zip: %s successfully", current.getFileName()));
        } catch (final IOException e) {
          Logger.info(
              String.format("Failed to Deleted Zip: %s successfully", current.getFileName()));
          e.printStackTrace();
        }
      } else {
        Logger.error(String.format("Could not delete Zip: %s!", current.getFileName()));
      }
      final int before = queue.size();
      queue.addAll(containsArchiveExtension(currentFolder));
      if (queue.size() == before) {
        currentFolder = folder;
      }
    }
  }

  @NotNull
  public static Set<Path> containsArchiveExtension(@NotNull final Path f) {
    final Set<Path> files = new HashSet<>();
    try (final Stream<Path> paths = Files.walk(f)) {
      paths.forEach(
          x -> {
            for (final String ext : ARCHIVE_EXTENSIONS) {
              if (x.getFileName().endsWith(ext)) {
                files.add(x);
              }
            }
          });
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return files;
  }

  @NotNull
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
    throw new UnsupportedOperationException(
        String.format("Cannot find Archive Extension for File! (%s)", name));
  }

  @NotNull
  public static String getFileName(@NotNull final String full) {
    if (full.endsWith(".tar.gz") || full.endsWith(".tar.xz")) {
      return full.substring(0, full.length() - 7);
    } else {
      return FilenameUtils.removeExtension(full);
    }
  }
}
