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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public abstract class AbstractConfiguration {

  private final DeluxeMediaPlugin plugin;
  private final String fileName;

  private final File configFile;
  private FileConfiguration fileConfiguration;

  public AbstractConfiguration(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String name) {
    this.plugin = plugin;
    fileName = name;
    configFile = new File(plugin.getDataFolder(), fileName);
  }

  public void reloadConfig() {
    fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
    final InputStream defConfigStream = plugin.getResource(fileName);
    if (defConfigStream != null) {
      final YamlConfiguration defConfig =
          YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
      fileConfiguration.setDefaults(defConfig);
    }
  }

  public FileConfiguration getConfig() {
    if (fileConfiguration == null) {
      reloadConfig();
    }
    return fileConfiguration;
  }

  public void saveConfig() {
    if (fileConfiguration != null && configFile != null) {
      try {
        getConfig().save(configFile);
      } catch (final IOException e) {
        plugin
            .getLogger()
            .log(Level.SEVERE, String.format("Could not save config to %s", configFile), e);
      }
    }
  }

  public void saveDefaultConfig() {
    if (!configFile.exists()) {
      plugin.saveResource(fileName, false);
    }
  }

  public void read() {
    if (!configFile.exists()) {
      saveDefaultConfig();
    }
    getConfig();
    serialize();
  }

  abstract void deserialize();

  abstract void serialize();

  public DeluxeMediaPlugin getPlugin() {
    return plugin;
  }

  public String getFileName() {
    return fileName;
  }

  public File getConfigFile() {
    return configFile;
  }

  public FileConfiguration getFileConfiguration() {
    return fileConfiguration;
  }
}
