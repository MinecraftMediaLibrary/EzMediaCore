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

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * The hosting provider used for generating urls to download a specific file. Useful for creating
 * custom providers and needing to generate links for them.
 */
public interface HostingSolution {

  /**
   * Generates a url for the specific requested file.
   *
   * @param file to generate parent directory of the HTTP Server for.
   * @return String url for the generated url to access the specific file.
   */
  @NotNull
  String generateUrl(@NotNull final String file);

  /**
   * Generates a url for the specific requested file.
   *
   * @param path to gnerate parent directory of the HTTP Server for.
   * @return String url for the generated url to access the specific file.
   */
  @NotNull
  String generateUrl(@NotNull final Path path);
}
