/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.playlist.spotify.SpotifyClient;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LibraryProvider {

  private static final Map<Plugin, WeakReference<MediaLibraryCore>> instantiated;

  static {
    instantiated = new WeakHashMap<>();
  }

  private Plugin plugin;
  private LibraryLoader loader;
  private Path libraryPath;
  private Path dependencyPath;
  private Path httpServerPath;
  private Path vlcPath;
  private Path imagePath;
  private Path audioPath;
  private Path videoPath;
  private SpotifyClient client;

  LibraryProvider() {}

  public static LibraryProvider builder() {
    return new LibraryProvider();
  }

  public static boolean getLibraryStatus(@NotNull final Plugin plugin) {
    return instantiated.containsKey(plugin);
  }

  public static <T extends JavaPlugin> boolean getLibraryStatus(@NotNull final Class<T> clazz) {
    return instantiated.containsKey(JavaPlugin.getPlugin(clazz));
  }

  public static MediaLibraryCore getLibraryInstance(@NotNull final Plugin plugin) {
    return instantiated.get(plugin).get();
  }

  private static MediaLibraryCore checkValidity(
      final Plugin plugin, final MediaLibraryCore library) {
    if (instantiated.containsKey(plugin)) {
      return instantiated.get(plugin).get();
    }
    instantiated.put(plugin, new WeakReference<>(library));
    return library;
  }

  public LibraryProvider plugin(@NotNull final Plugin plugin) {
    this.plugin = plugin;
    return this;
  }

  public LibraryProvider loader(@Nullable final LibraryLoader loader) {
    this.loader = loader;
    return this;
  }

  public LibraryProvider libraryPath(@Nullable final Path libraryPath) {
    this.libraryPath = libraryPath;
    return this;
  }

  public LibraryProvider dependencyPath(@Nullable final Path dependencyPath) {
    this.dependencyPath = dependencyPath;
    return this;
  }

  public LibraryProvider httpServerPath(@Nullable final Path httpServerPath) {
    this.httpServerPath = httpServerPath;
    return this;
  }

  public LibraryProvider vlcPath(@Nullable final Path vlcPath) {
    this.vlcPath = vlcPath;
    return this;
  }

  public LibraryProvider imagePath(@Nullable final Path imagePath) {
    this.imagePath = imagePath;
    return this;
  }

  public LibraryProvider audioPath(@Nullable final Path audioPath) {
    this.audioPath = audioPath;
    return this;
  }

  public LibraryProvider videoPath(@Nullable final Path videoPath) {
    this.videoPath = videoPath;
    return this;
  }

  public LibraryProvider spotifyClient(@NotNull final SpotifyClient client) {
    this.client = client;
    return this;
  }

  public MediaLibraryCore build() {
    final MediaLibraryCore core =
        new EzMediaCore(
            this.plugin,
            this.loader,
            this.libraryPath,
            this.dependencyPath,
            this.httpServerPath,
            this.vlcPath,
            this.imagePath,
            this.audioPath,
            this.videoPath,
            this.client);
    instantiated.put(this.plugin, new WeakReference<>(core));
    return core;
  }
}
