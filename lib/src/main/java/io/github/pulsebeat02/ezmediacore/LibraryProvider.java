package io.github.pulsebeat02.ezmediacore;

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
            this.videoPath);
    instantiated.put(this.plugin, new WeakReference<>(core));
    return core;
  }
}
