package io.github.pulsebeat02.deluxemediaplugin.json;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DataProvider<T> {

  private final DeluxeMediaPlugin plugin;
  private final String name;
  private final Path path;
  private T object;

  public DataProvider(@NotNull final DeluxeMediaPlugin plugin, @NotNull final String name)
      throws IOException {
    this.plugin = plugin;
    this.name = name;
    this.path = plugin.getBootstrap().getDataFolder().toPath().resolve(this.name);
  }

  public void deserialize() throws IOException {
    GsonProvider.getGson().toJson(this.object, Files.newBufferedWriter(this.path));
  }

  public void serialize() throws IOException {
    if (!Files.exists(this.path)) {
      this.plugin.getBootstrap().saveResource(this.name, false);
    }
    this.object =
        (T)
            GsonProvider.getGson()
                .fromJson(Files.newBufferedReader(this.path), this.object.getClass());
  }

  public @Nullable T getSerializedValue() {
    return this.object;
  }

  public @NotNull DeluxeMediaPlugin getPlugin() {
    return this.plugin;
  }

  public @NotNull String getFileName() {
    return this.name;
  }

  public @NotNull Path getConfigFile() {
    return this.path;
  }
}
