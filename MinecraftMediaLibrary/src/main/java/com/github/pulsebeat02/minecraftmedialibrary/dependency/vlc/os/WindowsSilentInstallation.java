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
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ArchiveUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VLCUtilities;
import com.sun.jna.NativeLibrary;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * The Windows specific silent installation. It uses a zip file hosted on Github to install the VLC
 * binaries, extracts the file, and loads them properly onto VLC.
 */
public class WindowsSilentInstallation extends SilentOSDependentSolution {

  private String vlcPath;

  /**
   * Instantiates a new WindowsSilentInstallation.
   *
   * @param library the library
   */
  public WindowsSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  /**
   * Instantiates a new WindowsSilentInstallation.
   *
   * @param dir the directory
   */
  public WindowsSilentInstallation(@NotNull final Path dir) {
    super(dir);
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final Path dir = getDir();
    Logger.info("No VLC Installation found on this Computer. Proceeding to a manual install.");
    final File zip = dir.resolve("VLC.zip").toFile();
    FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), zip);
    final String path = zip.getAbsolutePath();
    final File dirFile = dir.toFile();
    ArchiveUtilities.decompressArchive(new File(path), dirFile);
    vlcPath = new File(zip.getParentFile(), "vlc-3.0.12").getAbsolutePath();
    Logger.info(String.format("Successfully Extracted File (%s)", path));
    deleteArchive(zip);
    loadNativeDependency(dirFile);
    printSystemEnvironmentVariables();
    printSystemProperties();
  }

  @Override
  public void loadNativeDependency(@NotNull final File folder) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
    VLCUtilities.checkVLCExistence(folder);
  }

  /**
   * Gets the VLC path.
   *
   * @return the vlc path
   */
  public String getVlcPath() {
    return vlcPath;
  }
}
