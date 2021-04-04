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
   * @param file the file
   * @param isDebian whether package is Debian or not
   */
  public JuNestInstaller(
      @NotNull final String baseDirectory, @NotNull final File file, final boolean isDebian) {
    super(file);
    this.isDebian = isDebian;
    this.baseDirectory = baseDirectory;
    if (new File(baseDirectory, "scripts").mkdir()) {
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
    final File script = new File(baseDirectory + "scripts/vlc-installation.sh");
    createFile(script, "Made VLC Installation Script");
    Files.write(
        script.toPath(),
        getBashScript(getFile().getAbsolutePath()).getBytes(),
        StandardOpenOption.CREATE);
    RuntimeUtilities.executeBashScript(script, new String[] {}, "Successfully installed VLC Package");
  }

  private String getBashScript(@NotNull final String path) {
    final StringBuilder sb = new StringBuilder(baseDirectory + "junest-master/bin/junest setup \n");
    sb.append(new File(baseDirectory + "junest-master/bin/junest").getAbsolutePath())
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
    downloadConda();
    Logger.info("Setting up JuNest!");
    downloadJuNest();
    Logger.info("Setting up Paths!");
    setPaths();
  }

  /**
   * Installs RPM package manager.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void downloadConda() throws IOException {
    final File conda = new File(baseDirectory + "scripts/conda.sh");
    FileUtils.copyURLToFile(
        new URL(
            "https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh"
                + RuntimeUtilities.getCpuArch()),
        conda);
    RuntimeUtilities.executeBashScript(
        conda, new String[] {"-b -p linux-image/conda"}, "Successfully Installed Conda");
    RuntimeUtilities.executeBashScript(
        conda,
        new String[] {"install", "-c", "conda-forge", "curl"},
        "Successfully Installed cURL");
  }

  /**
   * Installs and extracts JuNest into the proper directory.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void downloadJuNest() throws IOException {
    final File junest = new File(baseDirectory + "junest.zip");
    FileUtils.copyURLToFile(
        new URL("https://github.com/PulseBeat02/junest/archive/refs/heads/master.zip"), junest);
    ArchiveUtilities.decompressArchive(junest, junest.getParentFile());
  }

  /**
   * Sets the proper paths for JuNest bash commands to function.
   *
   * @throws IOException if an exception occurred while fetching the url or file
   */
  private void setPaths() throws IOException {
    final File script = new File(baseDirectory + "scripts/junest-installation.sh");
    createFile(script, "Made JuNest Script");
    Files.write(
        script.toPath(),
        ResourceUtilities.getFileContents("script/junest.sh").getBytes(),
        StandardOpenOption.CREATE);
    RuntimeUtilities.executeBashScript(script, new String[] {}, "Successfully installed JuNest");
  }

  /**
   * Creates new file with specified message when successful.
   *
   * @param file the file
   * @param successful the successful message
   */
  private void createFile(@NotNull final File file, @NotNull final String successful) {
    try {
      if (file.getParentFile().mkdirs()) {
        Logger.info("Created Directories for File (" + file.getAbsolutePath() + ")");
      }
      if (file.createNewFile()) {
        Logger.info(successful);
      }
    } catch (final IOException e) {
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
