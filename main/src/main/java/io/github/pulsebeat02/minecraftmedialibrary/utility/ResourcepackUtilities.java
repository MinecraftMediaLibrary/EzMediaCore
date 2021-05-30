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

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackFormatVersioning;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Special resourcepack utilities used throughout the library and also open to users. Used for
 * easier resourcepack management.
 */
public final class ResourcepackUtilities {

  private ResourcepackUtilities() {}

  /**
   * Validate pack format.
   *
   * @param format the format
   * @return pack format validation
   */
  public static boolean validatePackFormat(final int format) {
    for (final PackFormatVersioning version : PackFormatVersioning.values()) {
      if (format == version.getPackFormatID()) {
        Logger.info(String.format("Pack Format Supported! (%d)", format));
        return true;
      }
    }
    Logger.warn(String.format("Pack Format Not Supported! (%d)", format));
    return false;
  }

  /**
   * Validate resourcepack icon.
   *
   * @param icon the icon
   * @return resourcepack icon validation
   */
  public static boolean validateResourcepackIcon(@NotNull final Path icon) {
    final String name = PathUtilities.getName(icon);
    final boolean valid = name.endsWith(".png");
    final String path = icon.toAbsolutePath().toString();
    if (valid) {
      Logger.info(String.format("Resourcepack Icon Accepted! (%s)", path));
    } else {
      Logger.warn(String.format("Resourcepack Icon Not Supported! (%s)", path));
    }
    return name.endsWith(".png");
  }
}
