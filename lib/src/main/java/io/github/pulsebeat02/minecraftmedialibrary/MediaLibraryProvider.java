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

package io.github.pulsebeat02.minecraftmedialibrary;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MediaLibraryProvider {

  private MediaLibraryProvider() {}

  /**
   * Static builder to create a media library from plugin.
   *
   * @param plugin the plugin
   * @return a new MediaLibrary instance
   */
  public static MediaLibrary create(final Plugin plugin) {
    return new MinecraftMediaLibrary(plugin);
  }

  /**
   * Static builder to create a media library from plugin.
   *
   * @param plugin the plugin
   * @param http the http
   * @param libraryPath the library path
   * @param vlcPath the vlc path
   * @param imagePath the image path
   * @param audioPath the audio path
   * @param isUsingVLCJ if user is using VLCJ
   * @return a new MediaLibrary instance
   */
  public static MediaLibrary create(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      @Nullable final String imagePath,
      @Nullable final String audioPath,
      final boolean isUsingVLCJ) {
    return new MinecraftMediaLibrary(
        plugin, http, libraryPath, vlcPath, imagePath, audioPath, isUsingVLCJ);
  }
}
