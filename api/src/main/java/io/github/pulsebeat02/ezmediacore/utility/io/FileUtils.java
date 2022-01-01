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
package io.github.pulsebeat02.ezmediacore.utility.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
import org.jetbrains.annotations.NotNull;

public final class FileUtils {

  private FileUtils() {}

  public static @NotNull Path downloadImageFile(
      @NotNull final String url, @NotNull final Path folder) throws IOException {
    checkNotNull(url, "URL cannot be null!");
    checkNotNull(folder, "Folder cannot be null!");
    checkArgument(url.length() != 0, "URL cannot be null or empty!");
    final String filePath = "%s/%s.png".formatted(folder, UUID.randomUUID());
    try (final InputStream in = new URL(url).openStream()) {
      Files.copy(in, Path.of(filePath));
    }
    return Path.of(filePath);
  }

  public static void createFileExceptionally(@NotNull final Path file) {
    try {
      createFile(file);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void createFile(@NotNull final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    final Path parent = file.getParent();
    if (Files.notExists(parent)) {
      Files.createDirectories(file.getParent());
    }
    if (Files.notExists(file)) {
      Files.createFile(file);
    }
  }

  public static void createFileIfNotExistsExceptionally(@NotNull final Path file) {
    try {
      createFileIfNotExists(file);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean createFileIfNotExists(@NotNull final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.notExists(file)) {
      Files.createFile(file);
      return true;
    }
    return false;
  }

  public static void createDirectoryIfNotExistsExceptionally(@NotNull final Path file) {
    try {
      createDirectoryIfNotExists(file);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean createDirectoryIfNotExists(@NotNull final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.notExists(file)) {
      Files.createDirectory(file);
      return true;
    }
    return false;
  }

  public static void deleteIfFileExistsExceptionally(@NotNull final Path file) {
    try {
      deleteIfFileExists(file);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean deleteIfFileExists(@NotNull final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    if (Files.exists(file)) {
      Files.delete(file);
      return true;
    }
    return false;
  }

  public static void copyURLToFile(@NotNull final String url, @NotNull final Path path) {
    checkNotNull(url, "URL cannot be null!");
    checkNotNull(path, "Path cannot be null!");
    try (final ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
        final FileChannel channel = new FileOutputStream(path.toString()).getChannel()) {
      channel.transferFrom(in, 0, Long.MAX_VALUE);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static @NotNull String getFirstLine(@NotNull final Path file) throws IOException {
    checkNotNull(file, "Path cannot be null!");
    return Files.lines(file).findFirst().orElseThrow(NoSuchElementException::new);
  }
}
