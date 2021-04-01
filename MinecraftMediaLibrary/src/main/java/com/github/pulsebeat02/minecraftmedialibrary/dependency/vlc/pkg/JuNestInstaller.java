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

import com.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTask;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourceUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Installs a package that can be Debian or RPM based on the JuNest distribution. */
public class JuNestInstaller extends PackageBase {

  private final boolean isDebian;

  /**
   * Instantiates a new JuNestInstaller.
   *
   * @param file the file
   * @param isDebian whether package is Debian or not
   */
  public JuNestInstaller(@NotNull final File file, final boolean isDebian) {
    super(file);
    this.isDebian = isDebian;
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void installPackage() throws IOException {
    final String path = getFile().getAbsolutePath();
    if (isDebian) {
      Logger.info("Executing Command (junet -f && apt install " + path);
      new CommandTask(args("junest", "-f", "&&", "apt", "install", path), true);
    } else {
      Logger.info("Executing Command (junet -f && rpm -i " + path);
      new CommandTask(args("junest", "-f", "&&", "rpm", "-i", path), true);
    }
  }

  /**
   * Uses any steps to setup a package.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void setupPackage() throws IOException {
    Logger.info("Running Command Chain! (Setup)");
    final File junest = new File("~/.local/share/junest.zip");
    FileUtils.copyURLToFile(
        new URL("https://github.com/PulseBeat02/JuNest-Mirror/raw/main/junest-7.3.7.zip"), junest);
    ArchiveUtilities.decompressArchive(junest, junest.getParentFile());
    final Process p =
        new ProcessBuilder(
                "bash",
                Objects.requireNonNull(ResourceUtilities.getResourceAsFile("junest.sh"))
                    .getAbsolutePath())
            .start();
    try {
      if (p.waitFor() == 0) {
        Logger.info("Successfully installed JuNest");
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks if the package is Debian or not.
   *
   * @return if the package is Debian. Otherwise it must be RPG
   */
  public boolean isDebian() {
    return isDebian;
  }
}
