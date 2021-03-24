/*............................................................................................
 . Copyright © 2021 PulseBeat_02                                                             .
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
import com.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTask;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * The Mac specific silent installation for VLC. Mac is significantly harder to accomplish compared
 * to Windows and possibly just as hard for Linux. Due to permission and security measures the
 * operating system has, we must install the file from a dmg. The current implementation installs
 * the proper dmg to the computer, then mounts it by using a command. After that, it moves the .APP
 * file that is inside the mounted drive into the specified folder. Because .APP is actually a
 * folder, we must call the move folder method instead of the move file method. Next, we have to
 * call a command to change the permissions of the file we are able to access the binaries and use
 * them. We call the chmod command to do this and set the permissions to 755. Finally, we unmount
 * the disk by using another command and load the native .so libraries into VLCJ.
 */
public class MacSilentInstallation extends SilentOSDependentSolution {

  /**
   * Instantiates a new MacSilentInstallation.
   *
   * @param library the library
   */
  public MacSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  /**
   * Instantiates a new MacSilentInstallation.
   *
   * @param dir the directory
   */
  public MacSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(dir)) {
      Logger.info("Found VLC Library in Mac! No need to install into path.");
    } else {
      final File dmg = new File(dir, "VLC.dmg");
      final File diskPath = new File("/Volumes/VLC media player");
      FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), dmg);
      try {
        if (mountDiskImage(dmg) != 0) {
          throw new IOException("Could not Mount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      final File app = new File(dir, "VLC.app");
      FileUtils.copyDirectory(new File(diskPath, "VLC.app"), app);
      try {
        changePermissions(app.getAbsolutePath());
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      Logger.info("Moved File!");
      try {
        if (unmountDiskImage(diskPath.getAbsolutePath()) != 0) {
          throw new IOException("Could not Unmount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      Logger.info("Unmounting Disk Successfully");
      deleteArchive(dmg);
      Logger.info("Deleted DMG File");
    }
    loadNativeDependency(new File(dir));
  }

  /**
   * Mounts disk image from file.
   *
   * @param dmg disk image
   * @return result code
   * @throws IOException if dmg cannot be found
   * @throws InterruptedException waiting for process
   */
  private int mountDiskImage(@NotNull final File dmg) throws IOException, InterruptedException {
    final String[] command = {"/usr/bin/hdiutil", "attach", dmg.getAbsolutePath()};
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
