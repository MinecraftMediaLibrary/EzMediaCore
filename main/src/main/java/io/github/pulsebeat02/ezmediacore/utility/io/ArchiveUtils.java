/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

package io.github.pulsebeat02.ezmediacore.utility.io;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.pulsebeat02.jarchivelib.ArchiverFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class ArchiveUtils {

  private static final Map<Set<String>, String> ASSOCIATED_COMPRESSED_TYPES;
  private static final Set<String> ARCHIVE_EXTENSIONS;
  private static final Set<String> SPECIAL_EXTENSIONS;

  static {
    ASSOCIATED_COMPRESSED_TYPES =
        Map.of(
            Set.of("zip"), "zip",
            Set.of("deb", "ar"), "ar",
            Set.of("rpm", "cpio"), "cpio",
            Set.of("tgz", "tar.gz"), "tar gz");
    ARCHIVE_EXTENSIONS = Set.of("zip", "deb", "rpm", "tgz", "gz", "ar", "cpio", "bz2");
    SPECIAL_EXTENSIONS = Set.of(".tar.gz");
  }

  private ArchiveUtils() {}

  public static void decompressArchive(@NotNull final Path file, @NotNull final Path result)
      throws IOException {
    checkNotNull(file, "File cannot be null!");
    checkNotNull(result, "Result cannot be null!");
    final String[] types = getCompressedType(PathUtils.getName(file)).split(" ");
    (types.length == 1
            ? ArchiverFactory.createArchiver(types[0])
            : ArchiverFactory.createArchiver(types[0], types[1]))
        .extract(file.toFile(), result.toFile());
  }

  public static void decompressArchive(
      @NotNull final Path file,
      @NotNull final Path result,
      @NotNull final String type,
      @NotNull final String compression)
      throws IOException {
    checkNotNull(file, "File cannot be null!");
    checkNotNull(result, "Result cannot be null!");
    checkNotNull(type, "Type cannot be null!");
    checkNotNull(compression, "Compression cannot be null!");
    ArchiverFactory.createArchiver(type, compression).extract(file.toFile(), result.toFile());
  }

  public static void decompressArchive(
      @NotNull final Path file, @NotNull final Path result, @NotNull final String type)
      throws IOException {
    checkNotNull(file, "File cannot be null!");
    checkNotNull(result, "Result cannot be null!");
    checkNotNull(type, "Type cannot be null!");
    ArchiverFactory.createArchiver(type).extract(file.toFile(), result.toFile());
  }

  public static void recursiveExtraction(@NotNull final Path file, @NotNull final Path folder)
      throws IOException {
    checkNotNull(file, "File cannot be null!");
    checkNotNull(folder, "Folder cannot be null!");
    decompressArchive(file, folder);
    Path currentFolder = folder;
    final Queue<Path> queue = new LinkedList<>(containsArchiveExtension(currentFolder));
    queue.remove(file);
    while (!queue.isEmpty()) {
      final Path current = queue.remove();
      currentFolder = currentFolder.resolve(getFileName(PathUtils.getName(current)));
      decompressArchive(current, currentFolder);
      deleteFile(current, file);
      final int before = queue.size();
      queue.addAll(containsArchiveExtension(currentFolder));
      if (queue.size() == before) {
        currentFolder = folder;
      }
    }
  }

  private static void deleteFile(@NotNull final Path current, @NotNull final Path file)
      throws IOException {
    if (!current.toAbsolutePath().toString().equals(file.toAbsolutePath().toString())) {
      Files.delete(current);
    }
  }

  @NotNull
  public static Set<Path> containsArchiveExtension(@NotNull final Path f) throws IOException {
    checkNotNull(f, "File cannot be null!");
    final Set<Path> files = new HashSet<>();
    try (final Stream<Path> paths = Files.walk(f)) {
      paths.forEach(path -> addPaths(files, path));
    }
    return files;
  }

  private static void addPaths(@NotNull final Set<Path> files, @NotNull final Path path) {
    ARCHIVE_EXTENSIONS.stream()
        .filter(extension -> path.getFileName().endsWith(extension))
        .forEach(extension -> files.add(path));
  }

  @NotNull
  public static String getCompressedType(@NotNull final String name) {
    checkNotNull(name, "Name cannot be null!");
    for (final Set<String> keys : ASSOCIATED_COMPRESSED_TYPES.keySet()) {
      if (keys.stream().anyMatch(name::endsWith)) {
        return ASSOCIATED_COMPRESSED_TYPES.get(keys);
      }
    }
    throw new UnsupportedOperationException(
        "Cannot find Archive Extension for File! (%s)".formatted(name));
  }

  @NotNull
  public static String getFileName(@NotNull final String full) {
    checkNotNull(full, "Name cannot be null!");
    return SPECIAL_EXTENSIONS.stream().anyMatch(full::endsWith)
        ? full.substring(0, full.length() - 7)
        : com.google.common.io.Files.getNameWithoutExtension(full);
  }
}
