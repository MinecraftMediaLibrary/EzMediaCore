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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.LinuxSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.MacSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os.WindowsSilentInstallation;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * The main class of the VLC native dependency fetcher. It calls the correct classes and dependency
 * solution based on the working operating system of the environment. It should be noted that VLC
 * uses operating system dependent binaries, meaning that each operating system must be carefully
 * handled correctly in order to load the binaries properly.
 */
public class VLCNativeDependencyFetcher {

  /** Instantiates a new VLCNativeDependencyFetcher. */
  private final String dir;

  /**
   * Instantiates a new VLC Native Dependency Fetcher process.
   *
   * @param library the library
   */
  public VLCNativeDependencyFetcher(@NotNull final MinecraftMediaLibrary library) {
    dir = library.getVlcFolder();
  }

  /**
   * Instantiates a new VLC Native Dependency Fetcher process.
   *
   * @param dir the directory
   */
  public VLCNativeDependencyFetcher(@NotNull final String dir) {
    this.dir = dir;
  }

  /**
   * Download libraries.
   *
   * <p>Currently in progress! Not finished as I am trying to support other operating systems.
   */
  public void downloadLibraries() {
    Logger.info("Trying to find Native VLC Installation...");
    try {
      if (RuntimeUtilities.isLinux()) {
        new LinuxSilentInstallation(dir).downloadVLCLibrary();
      } else if (RuntimeUtilities.isWindows()) {
        new WindowsSilentInstallation(dir).downloadVLCLibrary();
      } else if (RuntimeUtilities.isMac()) {
        new MacSilentInstallation(dir).downloadVLCLibrary();
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
