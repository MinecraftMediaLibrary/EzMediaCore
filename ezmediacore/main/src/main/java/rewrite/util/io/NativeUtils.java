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
package rewrite.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeUtils {

  private static final String JNA_LIBRARY_PATH = "jna.library.path";
  private static final Path TEMPORARY_DIRECTORY;

  static {
    try {
      TEMPORARY_DIRECTORY = FileUtils.createTempDirectory("native-library-loader");
      FileUtils.deleteOnExit(TEMPORARY_DIRECTORY);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  private NativeUtils() {
    throw new UnsupportedOperationException();
  }

  public static void loadLibraryFromUrl(final String url) throws IOException {
    final String filename = FileUtils.getFileNameFromUrl(url);
    final Path downloaded = FileUtils.downloadFile(url, TEMPORARY_DIRECTORY);
    final Path parent = downloaded.getParent();
    final String raw = downloaded.toString();
    System.load(raw);
    addSearchPath(parent);
  }

  public static void loadLibraryFromJar(final String path) throws IOException {
    final String[] parts = path.split("/");
    final String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
    loadNativeBinary(copyNativeLibrary(path, filename));
  }

  private static void loadNativeBinary(final Path temp) throws IOException {
    try {
      final Path parent = temp.getParent();
      final String raw = temp.toString();
      System.load(raw);
      addSearchPath(parent);
    } finally {
      deleteNativeBinary(temp);
    }
  }

  private static void deleteNativeBinary(final Path temp) throws IOException {
    if (FileUtils.isPosixCompliant()) {
      Files.deleteIfExists(temp);
    } else {
      FileUtils.deleteOnExit(temp);
    }
  }

  private static Path copyNativeLibrary(final String path, final String filename)
      throws IOException {
    final Path temp = TEMPORARY_DIRECTORY.resolve(filename);
    try (final InputStream is = NativeUtils.class.getResourceAsStream(path)) {
      Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
    }
    return temp;
  }

  /**
   * Adds a search directory for JNA to the search path.
   *
   * @param directory the directory
   */
  public static void addSearchPath(final Path directory) {

    final String dir = directory.toString();

    final String property = System.getProperty(JNA_LIBRARY_PATH); // only single path
    if (property == null || property.equals("null")) {
      System.setProperty(JNA_LIBRARY_PATH, dir);
      return;
    }

    final String libraryProperty = System.getProperty(JNA_LIBRARY_PATH);
    final String[] paths = libraryProperty.split(";");
    for (final String path : paths) {
      if (path.equals(dir)) {
        return; // already included
      }
    }

    final String path = property + File.pathSeparator + dir;
    System.setProperty(JNA_LIBRARY_PATH, path);
  }
}
