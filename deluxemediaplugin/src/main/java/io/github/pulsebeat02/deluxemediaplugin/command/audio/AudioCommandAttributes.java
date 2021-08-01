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

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public final class AudioCommandAttributes {

  private final AtomicBoolean completion;
  private final String soundKey;

  private Path resourcepackAudio;
  private String resourcepackLink;
  private byte[] resourcepackHash;

  public AudioCommandAttributes(@NotNull final DeluxeMediaPlugin plugin) {
    completion = new AtomicBoolean(false);
    soundKey = plugin.getName().toLowerCase();
  }

  public AtomicBoolean getCompletion() {
    return completion;
  }

  public void setCompletion(final boolean mode) {
    completion.set(mode);
  }

  public String getSoundKey() {
    return soundKey;
  }

  public Path getResourcepackAudio() {
    return resourcepackAudio;
  }

  public void setResourcepackAudio(final Path resourcepackAudio) {
    this.resourcepackAudio = resourcepackAudio;
  }

  public String getResourcepackLink() {
    return resourcepackLink;
  }

  public void setResourcepackLink(final String resourcepackLink) {
    this.resourcepackLink = resourcepackLink;
  }

  public byte[] getResourcepackHash() {
    return resourcepackHash;
  }

  public void setResourcepackHash(final byte[] resourcepackHash) {
    this.resourcepackHash = resourcepackHash;
  }
}
