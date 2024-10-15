/*
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
package io.github.pulsebeat02.ezmediacore.util.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Stream;

public final class FileUtils {

  private FileUtils() {
    throw new UnsupportedOperationException();
  }

  public static Path downloadImage(final String url, final Path folder) {
    final String name = getFileNameFromUrl(url);
    final Path path = folder.resolve(name);
    final URI uri = URI.create(url);
    try (final InputStream in = uri.toURL().openStream()) {
      Files.copy(in, path);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
    return path;
  }

  public static byte[] readAllBytes(final Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static Path createTempFile(final String prefix, final String suffix) {
    try {
      final String temp = System.getProperty("java.io.tmpdir");
      final String name = prefix + System.nanoTime() + suffix;
      final Path file = Path.of(temp, name);
      return Files.createFile(file);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static boolean createFile( final Path file) {
    try {
      final Path parent = file.getParent();
      createDirectoryIfNotExists(parent);
      createFileIfNotExists(file);
      return true;
    } catch (final IOException e) {
      return false;
    }
  }

  public static boolean createFileIfNotExists(final Path file) throws IOException {
    if (Files.notExists(file)) {
      Files.createFile(file);
      return true;
    }
    return false;
  }

  public static boolean createDirectoryIfNotExistsExceptionally( final Path file) {
    try {
      createDirectoryIfNotExists(file);
      return true;
    } catch (final IOException e) {
      return false;
    }
  }

  public static boolean createDirectoryIfNotExists( final Path file) throws IOException {
    if (Files.notExists(file)) {
      Files.createDirectory(file);
    }
    return true;
  }

  public static boolean deleteIfFileExists( final Path file) throws IOException {
    if (Files.exists(file)) {
      Files.delete(file);
    }
    return true;
  }

  public static boolean copyFromResourcesExceptionally(
       final String resource,  final Path destination) {
    final InputStream stream = requireNonNull(FileUtils.class.getResourceAsStream(resource));
    try {
      Files.copy(stream, destination);
      return true;
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static boolean isPosixCompliant() {
    try {
      final FileSystem defaultSystem = FileSystems.getDefault();
      final Set<String> supported = defaultSystem.supportedFileAttributeViews();
      return supported.contains("posix");
    } catch (final FileSystemNotFoundException | ProviderNotFoundException | SecurityException e) {
      return false;
    }
  }

  public static Path createTempDirectory(final String prefix) throws IOException {
    final String temp = System.getProperty("java.io.tmpdir");
    final String name = prefix + System.nanoTime();
    final Path directory = Path.of(temp, name);
    return Files.createDirectory(directory);
  }

  public static void deleteOnExit(final Path path) {
    final Runtime runtime = Runtime.getRuntime();
    final Thread hook = new Thread(() -> deleteFolderRecursively(path));
    runtime.addShutdownHook(hook);
  }

  public static void deleteFolderRecursively(final Path path) {
    try (final Stream<Path> stream = Files.walk(path).parallel()) {
      stream.filter(Files::isRegularFile).forEach(FileUtils::deleteFileExceptionally);
      Files.deleteIfExists(path);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static void deleteFileExceptionally(final Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static Path downloadFile(final String url, final Path dir) throws IOException {
    final URI uri = URI.create(url);
    final URL website = uri.toURL();
    final String name = getFileNameFromUrl(url);
    final Path target = dir.resolve(name);
    try (final InputStream in = website.openStream()) {
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
    return target;
  }

  public static String getFileNameFromUrl(final String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }
}
