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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.pkg;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * The PackageInstaller base which handles package installation depending on the file extension.
 * This depends on what package is being used and installs it accordingly.
 */
public abstract class PackageInstaller {

  private final LinuxPackage pkg;
  private final File file;

  /**
   * Instantiates a new PackageInstaller.
   *
   * @param pkg the package
   * @param file the file
   */
  public PackageInstaller(@NotNull final LinuxPackage pkg, @NotNull final File file) {
    this.pkg = pkg;
    this.file = file;
  }

  /**
   * Gets the Linux package.
   *
   * @return the linux package
   */
  public LinuxPackage getPkg() {
    return pkg;
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  public abstract void installPackage() throws IOException;

  /**
   * Uses any steps to setup a package.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  public abstract void setupPackage() throws IOException;

  /**
   * Creates an array from the arguments.
   *
   * @param array the arguments
   * @return the array
   */
  public String[] args(final String... array) {
    return array;
  }
}
