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
package io.github.pulsebeat02.ezmediacore.utility.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;


public final class FileUtils {

  private FileUtils() {}

  public static  Path downloadImageFile(
       final String url,  final Path folder) throws IOException {
    checkNotNull(url, "URL cannot be null!");
    checkNotNull(folder, "Folder cannot be null!");
    checkArgument(url.length() != 0, "URL cannot be null or empty!");
    final String filePath = downloadImage(url, folder);
    return Path.of(filePath);
  }


  private static String downloadImage( final String url,  final Path folder)
      throws IOException {
    final String filePath = "%s/%s.png".formatted(folder, UUID.randomUUID());
    try (final InputStream in = new URL(url).openStream()) {
      Files.copy(in, Path.of(filePath));
    }
    return filePath;
  }

  public static boolean createFile( final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    final Path parent = file.getParent();
    if (Files.notExists(parent)) {
      Files.createDirectories(file.getParent());
    }
    if (Files.notExists(file)) {
      Files.createFile(file);
    }
    return true;
  }

  public static boolean createFileIfNotExists( final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.notExists(file)) {
      Files.createFile(file);
      return true;
    }
    return false;
  }

  public static boolean createDirectoryIfNotExistsExceptionally( final Path file) {
    try {
      createDirectoryIfNotExists(file);
    } catch (final IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static boolean createDirectoryIfNotExists( final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.notExists(file)) {
      Files.createDirectory(file);
    }
    return true;
  }

  public static boolean deleteIfFileExists( final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.exists(file)) {
      Files.delete(file);
    }
    return true;
  }

  public static  String getContentExceptionally( final Path file) {
    checkNotNull(file, "Path cannot be null!");
    try {
      return new String(Files.readAllBytes(file));
    } catch (final IOException e) {
      return "";
    }
  }

  public static boolean setContentExceptionally( final Path file,  final String content) {
    checkNotNull(file, "Path cannot be null!");
    try {
      Files.write(file, content.getBytes());
      return true;
    } catch (final IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean copyFromResourcesExceptionally(
       final String resource,  final Path destination) {
    checkNotNull(resource, "Resource cannot be null!");
    checkNotNull(destination, "Destination cannot be null!");
    final InputStream stream = requireNonNull(FileUtils.class.getResourceAsStream(resource));
    try {
      Files.copy(stream, destination);
    } catch (final IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
