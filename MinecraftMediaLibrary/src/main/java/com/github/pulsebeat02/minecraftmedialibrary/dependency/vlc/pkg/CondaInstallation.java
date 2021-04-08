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

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Installs packages from the Conda package manager. Used for the JuNestInstaller as a helper class
 * to install the proper dependencies needed for JuNest.
 */
public class CondaInstallation {

  private final File conda;
  private final String baseDirectory;

  /**
   * Instantiates a new CondaInstallation.
   *
   * @param baseDirectory the base directory
   */
  public CondaInstallation(@NotNull final String baseDirectory) {
    this.baseDirectory = baseDirectory;
    conda = new File(baseDirectory, "scripts/conda.sh");
    try {
      setup();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Installs the package manager.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  public void setup() throws IOException {
    FileUtilities.createFile(conda, "Created conda.sh File");
    FileUtils.copyURLToFile(
        new URL(
            "https://github.com/PulseBeat02/Conda-Mirror/raw/main/Miniconda3-latest-Linux-x86_64.sh"),
        conda);
    final File condaFolder = new File(baseDirectory, "conda");
    if (!condaFolder.exists()) {
      if (condaFolder.mkdir()) {
        Logger.info("Made Conda Folder");
      }
    }
    RuntimeUtilities.executeBashScript(
        conda, new String[] {"-b -p linux-image/conda"}, "Successfully Installed Conda");
  }

  /**
   * Installs a specific package from the name.
   *
   * @param pkgName the package name
   */
  public void installPackage(@NotNull final String pkgName) {
    RuntimeUtilities.executeBashScript(
        conda,
        new String[] {"install", "-c", "conda-forge", pkgName},
        "Successfully Installed cURL");
  }

  /**
   * Gets the Conda script associated with the instance.
   *
   * @return the file script
   */
  public File getCondaScript() {
    return conda;
  }
}
