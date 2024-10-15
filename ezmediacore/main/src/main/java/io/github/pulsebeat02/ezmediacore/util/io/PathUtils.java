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

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;


public final class PathUtils {

  private PathUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   *
   *
   * <pre>
   * Checks if a string is a valid path.
   * Null safe.
   *
   * Calling examples:
   *    isValidPath("c:/test");      //returns true
   *    isValidPath("c:/te:t");      //returns false
   *    isValidPath("c:/te?t");      //returns false
   *    isValidPath("c/te*t");       //returns false
   *    isValidPath("good.txt");     //returns true
   *    isValidPath("not|good.txt"); //returns false
   *    isValidPath("not:good.txt"); //returns false
   * </pre>
   *
   * @param path the path
   * @return whether the path is valid
   */
  public static boolean isValidPath(final String path) {
    final String lowercase = path.toLowerCase(Locale.ROOT);
    if (isUrl(lowercase)) {
      return false;
    }
    return isValidFile(path);
  }

  private static boolean isValidFile(final String path) {
    try {
      final Path path1 = Path.of(path);
      return Files.exists(path1);
    } catch (final InvalidPathException e) {
      return false;
    }
  }

  private static boolean isUrl( final String lowercase) {
    return lowercase.startsWith("http://") || lowercase.startsWith("https://");
  }

  public static String getName( final Path path) {
    final Path name = path.getFileName();
    return name.toString();
  }
}
