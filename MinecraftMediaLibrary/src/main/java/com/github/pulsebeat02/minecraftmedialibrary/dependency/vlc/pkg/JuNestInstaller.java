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
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourceUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/** Installs a package that can be Debian or RPM based on the JuNest distribution. */
public class JuNestInstaller extends PackageBase {

  private final boolean isDebian;
  private final File scripts;

  /**
   * Instantiates a new JuNestInstaller.
   *
   * @param file the file
   * @param isDebian whether package is Debian or not
   */
  public JuNestInstaller(@NotNull final File file, final boolean isDebian) {
    super(file);
    this.isDebian = isDebian;
    scripts = new File("~/.local/share");
    if (scripts.mkdir()) {
      Logger.info("Made Scripts Directory");
    }
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void installPackage() throws IOException {
    final File script = new File(scripts, "installation.sh");
    Files.write(
        script.toPath(),
        getBashScript(getFile().getAbsolutePath()).getBytes(),
        StandardOpenOption.CREATE);
    executeBashScript(script, "Successfully installed VLC Package");
  }

  private String getBashScript(@NotNull final String path) {
    final StringBuilder sb =
        new StringBuilder(
            new File("~/.local/share/junest-7.3.7/bin/junest").getAbsolutePath() + " -f \n");
    if (isDebian) {
      sb.append("apt install ").append(path);
    } else {
      sb.append("rpm -i ").append(path);
    }
    return sb.toString();
  }

  /**
   * Uses any steps to setup a package.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void setupPackage() throws IOException {
    Logger.info("Setting up JuNest!");
    downloadJuNest();
    Logger.info("Setting up Paths!");
    setPaths();
  }

  /**
   * Installs and extracts JuNest into the proper directory.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void downloadJuNest() throws IOException {
    final File junest = new File("~/.local/share/junest.zip");
    FileUtils.copyURLToFile(
        new URL("https://github.com/PulseBeat02/JuNest-Mirror/raw/main/junest-7.3.7.zip"), junest);
    ArchiveUtilities.decompressArchive(junest, junest.getParentFile());
  }

  /**
   * Sets the proper paths for JuNest bash commands to function.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void setPaths() throws IOException {
    final File script = new File(scripts, "junest.sh");
    Files.write(
        script.toPath(),
        ResourceUtilities.getFileContents("script/junest.sh").getBytes(),
        StandardOpenOption.CREATE);
    executeBashScript(script, "Successfully installed JuNest");
  }

  /**
   * Executes a script in Linux.
   *
   * @param file the script
   * @param message the success message
   */
  private void executeBashScript(@NotNull final File file, @NotNull final String message) {
    try {
      final Process p = new ProcessBuilder("bash", file.getAbsolutePath()).start();
      if (p.waitFor() == 0) {
        Logger.info(message);
      } else {
        Logger.info("An issue occurred while running script! (" + file.getAbsolutePath() + ")");
        try (final BufferedReader b =
            new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
          final String line;
          if ((line = b.readLine()) != null) {
            Logger.info(line);
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    } catch (final InterruptedException | IOException e) {
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
