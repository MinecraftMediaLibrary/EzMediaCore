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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class MediaLibraryProvider {

  /** Instantiation set. Only one plugin should have one media library associated with such. */
  private static final Map<Plugin, WeakReference<MediaLibrary>> instantiated;

  static {
    instantiated = new WeakHashMap<>();
  }

  private MediaLibraryProvider() {}

  /**
   * Static builder to create a media library from plugin.
   *
   * @param plugin the plugin
   * @return a new MediaLibrary instance
   */
  public static MediaLibrary create(final Plugin plugin) {
    return checkValidity(plugin, new MinecraftMediaLibrary(plugin));
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
   * @param videoPath the video path
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
      @Nullable final String videoPath,
      final boolean isUsingVLCJ) {
    return checkValidity(
        plugin,
        new MinecraftMediaLibrary(
            plugin, http, libraryPath, vlcPath, imagePath, audioPath, videoPath, isUsingVLCJ));
  }

  /**
   * Checks if the plugin has the library instantiated.
   *
   * @param plugin the plugin
   * @return whether the library is instantiated for the plugin
   */
  public static boolean getLibraryStatus(@NotNull final Plugin plugin) {
    return instantiated.containsKey(plugin);
  }

  /**
   * Checks if the plugin has the library instantiated.
   *
   * @param clazz the plugin class
   * @return whether the library is instantiated for the plugin
   * @param <T> the type value
   */
  public static <T extends JavaPlugin> boolean getLibraryStatus(@NotNull final Class<T> clazz) {
    return instantiated.containsKey(JavaPlugin.getPlugin(clazz));
  }

  /**
   * Gets the library instance.
   *
   * @param plugin the plugin
   * @return the library instance
   */
  public static MediaLibrary getLibraryInstance(@NotNull final Plugin plugin) {
    return instantiated.get(plugin).get();
  }

  /**
   * Checks the validity of the plugin, and see if an instance was created.
   *
   * @param plugin the plugin
   * @param library the media library
   *
   * @return an instance of the MediaLibrary
   */
  private static MediaLibrary checkValidity(final Plugin plugin, final MediaLibrary library) {
    final String id = plugin.getName();
    if (instantiated.containsKey(plugin)) {
      throw new IllegalStateException(
          String.format(
              "Plugin %s cannot instantiate MinecraftMediaLibrary twice!", plugin.getName()));
    }
    instantiated.put(plugin, new WeakReference<>(library));
    return library;
  }
}
