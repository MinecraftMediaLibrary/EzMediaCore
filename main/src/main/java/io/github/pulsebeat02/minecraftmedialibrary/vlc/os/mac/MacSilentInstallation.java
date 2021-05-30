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

package io.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTask;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VLCUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.AbstractSilentOSDependentSolution;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.SilentInstallationType;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Mac specific silent installation for VLC. Mac is significantly harder to accomplish compared
 * to Windows and possibly just as hard for Linux. Due to permission and security measures the
 * operating system has, we must install the file from a dmg. The current implementation installs
 * the proper dmg to the computer, then mounts it by using a command. After that, it moves the .APP
 * file that is inside the mounted drive into the application folder. Because .APP is actually a
 * folder, we must call the move folder method instead of the move file method. Next, we have to
 * call a command to change the permissions of the file we are able to access the binaries and use
 * them. We call the chmod command to do this and set the permissions to 755. Finally, we unmount
 * the disk by using another command and load the native .so libraries into VLCJ.
 */
public class MacSilentInstallation extends AbstractSilentOSDependentSolution {

  /**
   * Instantiates a new MacSilentInstallation.
   *
   * @param library the library
   */
  public MacSilentInstallation(@NotNull final MediaLibrary library) {
    super(library);
  }

  /**
   * Instantiates a new MacSilentInstallation.
   *
   * @param dir the directory
   */
  public MacSilentInstallation(@NotNull final Path dir) {
    super(dir);
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final Path dir = getDir();
    final Path dmg = dir.resolve("VLC.dmg");
    final Path diskPath = Paths.get("/Volumes/VLC media player");
    FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), dmg.toFile());
    try {
      if (mountDiskImage(dmg.toAbsolutePath().toString()) != 0) {
        throw new RuntimeException("Could not Mount Disk File!");
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    final Path app = dir.resolve("VLC.app");
    FileUtils.copyDirectory(diskPath.resolve("VLC.app").toFile(), app.toFile());
    try {
      changePermissions(app.toAbsolutePath().toString());
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    Logger.info("Moved File!");
    try {
      if (unmountDiskImage(diskPath.toAbsolutePath().toString()) != 0) {
        throw new RuntimeException("Could not Unmount Disk File!");
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    Logger.info("Unmounting Disk Successfully");
    deleteArchive(dmg);
    Logger.info("Deleted DMG File");
    loadNativeDependency(app);
  }

  @Override
  public void loadNativeDependency(final @NotNull Path folder) {
    VLCUtilities.checkVLCExistence(folder);
  }

  @Override
  public SilentInstallationType getType() {
    return SilentInstallationType.LINUX;
  }

  /**
   * Mounts disk image from file.
   *
   * @param dmg disk image
   * @return result code
   * @throws IOException if dmg cannot be found
   * @throws InterruptedException waiting for process
   */
  private int mountDiskImage(@NotNull final String dmg) throws IOException, InterruptedException {
    final String[] command = {"/usr/bin/hdiutil", "attach", dmg};
    final CommandTask t = new CommandTask(command, true);
    Logger.info("============= DMG INFORMATION =============");
    Logger.info(t.getResult());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  /**
   * Unmounts disk image from file.
   *
   * @param path disk path
   * @return result code
   * @throws IOException if path cannot be found
   * @throws InterruptedException waiting for process
   */
  private int unmountDiskImage(@NotNull final String path)
      throws IOException, InterruptedException {
    final String[] command = {"diskutil", "unmount", path};
    final CommandTask t = new CommandTask(command, true);
    Logger.info("=========== UNMOUNT INFORMATION ===========");
    Logger.info(t.getResult());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  /**
   * Changes permission of app file.
   *
   * @param path path
   * @throws IOException if path couldn't be found
   * @throws InterruptedException waiting for process
   */
  private void changePermissions(@NotNull final String path)
      throws IOException, InterruptedException {
    final String[] command = {"chmod", "-R", "755", path};
    new CommandTask(command, true).getProcess().waitFor();
  }
}
