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

package com.github.pulsebeat02.minecraftmedialibrary.vlc.os.mac;

import com.github.pulsebeat02.minecraftmedialibrary.vlc.os.WellKnownDirectoryProvider;
import com.google.common.collect.ImmutableSet;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/** Well known Mac directory for VLC installation. */
public class MacKnownDirectories extends WellKnownDirectoryProvider {

  /** Instantiates a new MacKnownDirectories. */
  public MacKnownDirectories() {
    super(
        ImmutableSet.of(
            "/Applications/VLC.app/Contents/Frameworks",
            "/Applications/VLC.app/Contents/MacOS/lib"));
  }

  /**
   * Searches and returns a set of possible paths.
   *
   * @return the possible paths
   */
  @Override
  public Set<String> search() {
    final Set<String> paths = new HashSet<>();
    for (final String path : getDirectories()) {
      if (Files.exists(Paths.get(path))) {
        paths.add(path);
      }
    }
    return paths;
  }
}
