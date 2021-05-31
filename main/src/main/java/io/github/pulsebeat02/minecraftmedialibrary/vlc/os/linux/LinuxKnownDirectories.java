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

package io.github.pulsebeat02.minecraftmedialibrary.vlc.os.linux;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.minecraftmedialibrary.vlc.os.WellKnownDirectoryProvider;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/** Well known Linux directory for VLC installation. */
public class LinuxKnownDirectories extends WellKnownDirectoryProvider {

  /** Instantiates a new LinuxKnownDirectories. */
  public LinuxKnownDirectories() {
    super(
        ImmutableSet.of(
            "/usr/lib/x86_64-linux-gnu",
            "/usr/lib64",
            "/usr/local/lib64",
            "/usr/lib/i386-linux-gnu",
            "/usr/lib",
            "/usr/local/lib"));
  }

  /**
   * Searches and returns a set of possible paths.
   *
   * @return the possible paths
   */
  @Override
  public Set<String> search() {
    return getDirectories().stream()
        .filter(p -> Files.exists(Paths.get(p)))
        .collect(Collectors.toSet());
  }
}
