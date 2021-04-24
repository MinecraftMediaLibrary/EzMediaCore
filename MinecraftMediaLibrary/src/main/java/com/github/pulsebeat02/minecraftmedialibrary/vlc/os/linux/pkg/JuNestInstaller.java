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

package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.pkg;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ResourceUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/** Installs a package that can be Debian or RPM based on the JuNest distribution. */
public class JuNestInstaller extends PackageBase {

  private final boolean isDebian;
  private final String baseDirectory;

  /**
   * Instantiates a new JuNestInstaller.
   *
   * @param baseDirectory the base directory
   * @param file the file
   * @param isDebian whether package is Debian or not
   */
  public JuNestInstaller(
      @NotNull final String baseDirectory, @NotNull final File file, final boolean isDebian) {
    super(file, false);
    this.isDebian = isDebian;
    this.baseDirectory = baseDirectory;
    if (new File(baseDirectory, "scripts").mkdirs()) {
      Logger.info("Made Scripts Directory");
    }
    try {
      setupPackage();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Installs the packages accordingly.
   *
   * @throws IOException if an io issue has occurred during the process
   */
  @Override
  public void installPackage() throws IOException {
    Logger.info("Installing cURL Package...");
    new CondaInstallation(baseDirectory).installPackage("curl");
    Logger.info("Setting Up VLC Installation");
    final File script = new File(String.format("%s/scripts/vlc-installation.sh", baseDirectory));
    FileUtilities.createFile(script, "Made VLC Installation Script");
    Files.write(
        script.toPath(),
        getBashScript(getFile().getAbsolutePath()).getBytes(),
        StandardOpenOption.CREATE);
    RuntimeUtilities.executeBashScript(
        script, new String[] {}, "Successfully installed VLC Package");
  }

  /**
   * Creates the necessary bash script required for JuNest.
   *
   * @param path the path at which the script is located
   * @return the resulting script
   */
  @NotNull
  private String getBashScript(@NotNull final String path) {
    final StringBuilder sb =
        new StringBuilder(String.format("%s/junest-master/bin/junest setup \n", baseDirectory));
    sb.append(
            new File(String.format("%s/junest-master/bin/junest", baseDirectory)).getAbsolutePath())
        .append(" -f \n");
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
    Logger.info("Setting up JuNest...");
    downloadJuNest();
    Logger.info("Setting up Paths...");
    setJuNestPaths();
    Logger.info("JuNest Setup Completion");
  }

  /**
   * Installs and extracts JuNest into the proper directory.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void downloadJuNest() throws IOException {
    final File junest = new File(String.format("%s/junest.zip", baseDirectory));
    FileUtils.copyURLToFile(
        new URL("https://github.com/PulseBeat02/junest/archive/refs/heads/master.zip"), junest);
    ArchiveUtilities.decompressArchive(junest, junest.getParentFile());
  }

  /**
   * Sets the proper paths for JuNest bash commands to function.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void setJuNestPaths() throws IOException {
    final File script = new File(String.format("%s/scripts/junest-installation.sh", baseDirectory));
    FileUtilities.createFile(script, "Made JuNest Script");
    Files.write(
        script.toPath(),
        ResourceUtilities.getFileContents("script/junest.sh").getBytes(),
        StandardOpenOption.CREATE);
    RuntimeUtilities.executeBashScript(script, new String[] {}, "Successfully installed JuNest");
  }

  /**
   * Checks if the package is Debian or not.
   *
   * @return if the package is Debian. Otherwise it must be RPG
   */
  public boolean isDebian() {
    return isDebian;
  }

  /**
   * Gets the base directory of the installation.
   *
   * @return the base directory
   */
  public String getBaseDirectory() {
    return baseDirectory;
  }
}
