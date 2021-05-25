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

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Special file utilities used throughout the library and also open to users. Used for easier file
 * management.
 */
public final class FileUtilities {

  private FileUtilities() {}

  /**
   * Download image file from URL.
   *
   * @param url the url
   * @param path the path
   * @return the file
   */
  @NotNull
  public static File downloadImageFile(@NotNull final String url, @NotNull final Path path) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "URL cannot be null or empty!");
    final String filePath = String.format("%s/%s.png", path, UUID.randomUUID());
    try (final InputStream in = new URL(url).openStream()) {
      Files.copy(in, Paths.get(filePath));
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return new File(filePath);
  }

  /**
   * Creates new file with specified message when successful.
   *
   * @param file the file
   * @param successful the successful message
   */
  public static void createFile(@NotNull final File file, @NotNull final String successful) {
    try {
      if (file.getParentFile().mkdirs()) {
        Logger.info(String.format("Created Directories for File (%s)", file.getAbsolutePath()));
      }
      if (file.createNewFile()) {
        Logger.info(successful);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
