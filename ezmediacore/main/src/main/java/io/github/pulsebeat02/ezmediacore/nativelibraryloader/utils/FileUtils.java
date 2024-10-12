/**
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package io.github.pulsebeat02.ezmediacore.nativelibraryloader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public final class FileUtils {

  private FileUtils() {}

  public static boolean isPosixCompliant() {
    try {
      return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    } catch (final FileSystemNotFoundException | ProviderNotFoundException | SecurityException e) {
      return false;
    }
  }

  public static Path createTempDirectory(final String prefix) throws IOException {
    final String temp = System.getProperty("java.io.tmpdir");
    final Path directory = Paths.get(temp, prefix + System.nanoTime());
    Files.createDirectory(directory);
    return directory;
  }

  public static void deleteOnExit(final Path path) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteFolderRecursively(path)));
  }

  public static void deleteFolderRecursively(final Path path) {
    try (final Stream<Path> stream = Files.walk(path).parallel()) {
      stream.filter(Files::isRegularFile).forEach(FileUtils::deleteFileExceptionally);
      Files.deleteIfExists(path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void deleteFileExceptionally(final Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static Path downloadFile(final String url, final Path dir) throws IOException {
    final URL website = new URL(url);
    final Path target = dir.resolve(getFileNameFromUrl(url));
    try (final InputStream in = website.openStream()) {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
    return target;
  }

  public static String getFileNameFromUrl(final String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }
}
