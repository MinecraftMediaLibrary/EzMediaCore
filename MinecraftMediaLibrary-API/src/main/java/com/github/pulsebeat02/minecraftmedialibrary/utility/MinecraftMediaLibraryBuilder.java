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

package com.github.pulsebeat02.minecraftmedialibrary.utility;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A builder class which helps users create a MinecraftMediaLibrary instance to use. */
public class MinecraftMediaLibraryBuilder {

  private Plugin plugin;
  private boolean isUsingVLCJ = true;
  private String http = null;
  private String libraryPath = null;
  private String vlcPath = null;
  private String imagePath = null;
  private String audioPath = null;

  public MinecraftMediaLibraryBuilder setPlugin(@NotNull final Plugin plugin) {
    this.plugin = plugin;
    return this;
  }

  public MinecraftMediaLibraryBuilder setIsUsingVLCJ(final boolean isUsingVLCJ) {
    this.isUsingVLCJ = isUsingVLCJ;
    return this;
  }

  public MinecraftMediaLibraryBuilder setHttp(@Nullable final String http) {
    this.http = http;
    return this;
  }

  public MinecraftMediaLibraryBuilder setLibraryPath(@Nullable final String libraryPath) {
    this.libraryPath = libraryPath;
    return this;
  }

  public MinecraftMediaLibraryBuilder setVlcPath(final @Nullable String vlcPath) {
    this.vlcPath = vlcPath;
    return this;
  }

  public MinecraftMediaLibraryBuilder setImagePath(@Nullable final String imagePath) {
    this.imagePath = imagePath;
    return this;
  }

  public MinecraftMediaLibraryBuilder setAudioPath(@Nullable final String audioPath) {
    this.audioPath = audioPath;
    return this;
  }

  public MinecraftMediaLibrary build() {
    return new MinecraftMediaLibrary(
        plugin, http, libraryPath, vlcPath, imagePath, audioPath, isUsingVLCJ);
  }
}
