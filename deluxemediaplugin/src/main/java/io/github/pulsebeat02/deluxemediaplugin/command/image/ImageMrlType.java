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

package io.github.pulsebeat02.deluxemediaplugin.command.image;

import io.github.pulsebeat02.ezmediacore.utility.io.PathUtils;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public enum ImageMrlType {
  LOCAL_FILE,
  DIRECT_LINK;

  public static final Set<String> EXTENSIONS;

  static {
    EXTENSIONS = Set.of("png", "jpg", "jpeg", "tif", "gif");
  }

  public static Optional<ImageMrlType> getType(@NotNull final String mrl) {
    return isMrlLocalFile(mrl)
        ? Optional.of(LOCAL_FILE)
        : isMrlDirectLink(mrl) ? Optional.of(DIRECT_LINK) : Optional.empty();
  }

  private static boolean isMrlLocalFile(@NotNull final String mrl) {
    try {
      final Path path = Path.of(mrl);
      return Files.exists(path) && matchesFileType(PathUtils.getName(path));
    } catch (final InvalidPathException e) {
      return false;
    }
  }

  private static boolean isMrlDirectLink(@NotNull final String mrl) {
    return mrl.startsWith("http") && matchesFileType(mrl);
  }

  private static boolean matchesFileType(@NotNull final String mrl) {
    return EXTENSIONS.stream()
        .anyMatch(extension -> StringUtils.endsWithIgnoreCase(mrl, extension));
  }
}
