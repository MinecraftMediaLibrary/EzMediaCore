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

package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows;

import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.WellKnownDirectoryProvider;
import com.google.common.collect.ImmutableSet;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.Set;

/** Well known Windows directory for VLC installation. */
public class WindowsKnownDirectories extends WellKnownDirectoryProvider {

  /** Instantiates a new WindowsKnownDirectories. */
  public WindowsKnownDirectories() {
    super(ImmutableSet.of());
  }

  /**
   * Searches and returns a set of possible paths.
   *
   * @return the possible paths
   */
  @Override
  public Set<String> search() {
    try {
      return ImmutableSet.of(
          Advapi32Util.registryGetStringValue(
              WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\VideoLAN\\VLC", "InstallDir"));
    } catch (final Exception e) {
      return ImmutableSet.of();
    }
  }
}
