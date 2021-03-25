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
import com.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTaskChain;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.LinuxPackage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

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
      new CommandTask(args("junest", "-f", "&&", "apt", "install", path), true);
    } else {
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
    new CommandTaskChain()
        .addTask(
            new CommandTask(
                args(
                    "git", "clone", "git://github.com/fsquillace/junest", "~/.local/share/junest")))
        .addTask(new CommandTask(args("export", "PATH=~/.local/share/junest/bin:$PATH")))
        .addTask(new CommandTask(args("export", "PATH=\"$PATH:~/.junest/usr/bin_wrappers\"")))
        .run();
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
