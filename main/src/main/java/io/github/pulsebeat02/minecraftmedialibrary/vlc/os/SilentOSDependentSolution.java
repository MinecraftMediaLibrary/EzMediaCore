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

package io.github.pulsebeat02.minecraftmedialibrary.vlc.os;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * An abstract class used to install and load VLC silently into the user. It depends for each
 * operating system, however, this class is used to handle certain broilerplate code in an easier
 * fashion.
 */
public interface SilentOSDependentSolution {
  /**
   * Downloads VLC dependencies.
   *
   * @throws IOException if an issue occured during installation
   */
  void downloadVLCLibrary() throws IOException;

  /**
   * Loads native dependency from file.
   *
   * @param folder directory
   */
  void loadNativeDependency(@NotNull final Path folder);

  /** Prints all System environment variables. */
  void printSystemEnvironmentVariables();

  /** Prints all System properties. */
  void printSystemProperties();

  /**
   * Gets VLC folder in folder.
   *
   * @param folder search folder
   * @return file
   */
  @Nullable
  File findVLCFolder(@NotNull final File folder);

  /**
   * Deletes file (archive).
   *
   * @param zip archive
   */
  void deleteArchive(@NotNull final Path zip);

  /**
   * Gets directory of file.
   *
   * @return directory
   */
  Path getDir();

  SilentInstallationType getType();
}
