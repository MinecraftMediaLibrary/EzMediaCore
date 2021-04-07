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

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceUtilities {

  /**
   * Gets resource from file in resources folder.
   *
   * @return contents in String format
   * @throws IOException if file couldn't be found
   */
  public static String getFileContents(@NotNull final String name) throws IOException {
    final ClassLoader loader = ResourceUtilities.class.getClassLoader();
    final InputStream input = loader.getResourceAsStream(name);
    if (input == null) {
      throw new IllegalArgumentException("File not Found! " + name);
    } else {
      return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }
  }

  /**
   * Gets the resource from path as file in resources folder.
   *
   * @param resourcePath path
   * @return file object
   */
  public static File getResourceAsFile(@NotNull final String resourcePath) {
    try {
      final InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
      if (in == null) {
        return null;
      }
      final File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
      tempFile.deleteOnExit();
      try (final FileOutputStream out = new FileOutputStream(tempFile)) {
        final byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }
      return tempFile;
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Access a resource file from inside the jar.
   *
   * @param resource the resource name
   * @return the inputstream of the file
   */
  public static InputStream accessResourceFileJar(@NotNull final String resource) {
    InputStream input = ResourceUtilities.class.getResourceAsStream("/resources/" + resource);
    if (input == null) {
      input = ResourceUtilities.class.getClassLoader().getResourceAsStream(resource);
    }
    return input;
  }
}
