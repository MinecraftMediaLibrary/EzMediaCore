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

package io.github.pulsebeat02.minecraftmedialibrary.utility;

import io.github.pulsebeat02.minecraftmedialibrary.exception.UnsupportedOperatingSystemException;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.MMLNativeDiscovery;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.WellKnownDirectoryProvider;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.LinuxKnownDirectories;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux.LinuxNativeDiscovery;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac.MacKnownDirectories;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac.MacNativeDiscovery;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows.WindowsKnownDirectories;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.windows.WindowsNativeDiscovery;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class VLCUtilities {

  private VLCUtilities() {}

  /**
   * Checks if VLC installation exists or not.
   *
   * @param directory the library
   * @return whether vlc can be found or not
   */
  public static boolean checkVLCExistence(@NotNull final File directory) {
    MMLNativeDiscovery discovery = null;
    WellKnownDirectoryProvider provider = null;
    if (RuntimeUtilities.isWindows()) {
      discovery = new WindowsNativeDiscovery();
      provider = new WindowsKnownDirectories();
    } else if (RuntimeUtilities.isMac()) {
      discovery = new MacNativeDiscovery();
      provider = new MacKnownDirectories();
    } else if (RuntimeUtilities.isLinux()) {
      discovery = new LinuxNativeDiscovery();
      provider = new LinuxKnownDirectories();
    }
    if (discovery == null) {
      throw new UnsupportedOperatingSystemException(
          "Couldn't find the correct method to discover VLC!");
    }
    final List<String> paths = new ArrayList<>(provider.search());
    paths.add(0, directory.getAbsolutePath());
    for (final String path : paths) {
      if (discovery.discover(new File(path))) {
        return true;
      }
    }
    // last resort
    return new NativeDiscovery().discover();
  }
}
