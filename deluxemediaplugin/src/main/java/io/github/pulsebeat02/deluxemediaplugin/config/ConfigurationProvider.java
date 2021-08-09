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

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigurationProvider {

  private final DeluxeMediaPlugin plugin;
  private final String name;
  private final Path config;

  private FileConfiguration fileConfiguration;

  public ConfigurationProvider(@NotNull final DeluxeMediaPlugin plugin, @NotNull final String name)
      throws IOException {
    this.plugin = plugin;
    this.name = name;
    this.config = Path.of(plugin.getDataFolder().toString()).resolve(this.name);
  }

  public void reloadConfig() {
    this.fileConfiguration = YamlConfiguration.loadConfiguration(this.config.toFile());
    final InputStream defConfigStream = this.plugin.getResource(this.name);
    if (defConfigStream != null) {
      final YamlConfiguration defConfig =
          YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
      this.fileConfiguration.setDefaults(defConfig);
    }
  }

  public FileConfiguration getConfig() {
    if (this.fileConfiguration == null) {
      this.reloadConfig();
    }
    return this.fileConfiguration;
  }

  public void saveConfig() {
    if (this.fileConfiguration != null && this.config != null) {
      try {
        this.getConfig().save(this.config.toFile());
      } catch (final IOException e) {
        this.plugin
            .getLogger()
            .log(Level.SEVERE, "Could not save config to %s".formatted(this.config), e);
      }
    }
  }

  public void saveDefaultConfig() {
    if (!Files.exists(this.config)) {
      this.plugin.saveResource(this.name, false);
    }
  }

  public void read() {
    if (!Files.exists(this.config)) {
      this.saveDefaultConfig();
    }
    this.getConfig();
    try {
      this.serialize();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  abstract void deserialize();

  abstract void serialize() throws IOException;

  public DeluxeMediaPlugin getPlugin() {
    return this.plugin;
  }

  public String getFileName() {
    return this.name;
  }

  public Path getConfigFile() {
    return this.config;
  }

  public FileConfiguration getFileConfiguration() {
    return this.fileConfiguration;
  }
}
