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

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public final class PathUtils {

  private PathUtils() {}

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
  public static boolean isValidPath(@NotNull final String path) {
    checkNotNull(path, "Path cannot be null!");
    final String lowercase = path.toLowerCase(Locale.ROOT);
    final String http = "http";
    if (lowercase.startsWith("%s://".formatted(http))
        || lowercase.startsWith("%ss://".formatted(http))) {
      return false;
    }
    return Files.exists(Path.of(path));
  }

  public static @NotNull String getName(@NotNull final Path path) {
    checkNotNull(path, "Path cannot be null!");
    return path.getFileName().toString();
  }
}
