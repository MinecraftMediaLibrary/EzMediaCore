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

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceUtilities {

  private ResourceUtilities() {}

  /**
   * Gets resource from file in resources folder.
   *
   * @param name the name of the file
   * @return contents in String format
   * @throws IOException if file couldn't be found
   */
  @NotNull
  public static String getFileContents(@NotNull final String name) throws IOException {
    final ClassLoader loader = ResourceUtilities.class.getClassLoader();
    final InputStream input = loader.getResourceAsStream(name);
    if (input == null) {
      throw new NullPointerException(String.format("File not Found! %s", name));
    } else {
      return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }
  }

  /**
   * Access a resource file from inside the jar.
   *
   * @param resource the resource name
   * @return the inputstream of the file
   */
  @Nullable
  public static InputStream accessResourceFileJar(@NotNull final String resource) {
    InputStream input =
        ResourceUtilities.class.getResourceAsStream(String.format("/resources/%s", resource));
    if (input == null) {
      input = ResourceUtilities.class.getClassLoader().getResourceAsStream(resource);
    }
    return input;
  }
}
