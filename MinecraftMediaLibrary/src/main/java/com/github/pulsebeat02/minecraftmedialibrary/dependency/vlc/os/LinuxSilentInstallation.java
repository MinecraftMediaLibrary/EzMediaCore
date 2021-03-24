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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackageManager;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * The Linux specific installation for VLC is hard in the sense that we must specify the correct
 * packages to be used for each distribution. Our handling class LinuxPackageManager makes this
 * class's job much easier. It creates an instance of the LinuxPackageManager class, gets the needed
 * package, extracts the contents, and then loads the binaries into the runtime.
 */
public class LinuxSilentInstallation extends SilentOSDependentSolution {

  /**
   * Instantiates a new LinuxSilentInstallation.
   *
   * @param library the library.
   */
  public LinuxSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  /**
   * Instantiates a new LinuxSilentInstallation.
   *
   * @param dir the directory.
   */
  public LinuxSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(dir)) {
      Logger.info("Found VLC Library in Linux! No need to install into path.");
    } else {
      Logger.info("No VLC Installation found on this Computer. Proceeding to a manual install.");
      final LinuxPackageManager manager = new LinuxPackageManager(dir);
      final File f = manager.getPackage();
      manager.extractContents();
      Logger.info("Downloaded and Extracted Package (" + f.getAbsolutePath() + ")");
      deleteArchive(f);
      loadNativeDependency(new File(dir));
      printSystemEnvironmentVariables();
      printSystemProperties();
    }
  }
}
