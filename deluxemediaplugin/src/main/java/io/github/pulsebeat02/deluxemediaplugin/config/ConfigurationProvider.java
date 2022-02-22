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

package io.github.pulsebeat02.deluxemediaplugin.config;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.utility.nullability.Nill;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigurationProvider<T> implements ConfigHolder<T> {

  private final DeluxeMediaPlugin plugin;
  private final JavaPlugin loader;
  private final String name;
  private final Path config;

  private FileConfiguration fileConfiguration;

  public ConfigurationProvider(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final String name) {
    this.plugin = plugin;
    this.name = name;
    this.loader = plugin.getBootstrap();
    this.config = this.loader.getDataFolder().toPath().resolve(this.name);
  }

  @Override
  public void reloadConfig() {
    this.fileConfiguration = YamlConfiguration.loadConfiguration(this.config.toFile());
    final InputStream defConfigStream = this.loader.getResource(this.name);
    Nill.ifNot(defConfigStream, () -> this.setConfiguration(defConfigStream));
  }

  private void setConfiguration(@NotNull final InputStream defConfigStream) {
    this.fileConfiguration.setDefaults(
        YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
  }

  @Override
  public @NotNull FileConfiguration getConfig() {
    if (this.fileConfiguration == null) {
      this.reloadConfig();
    }
    return this.fileConfiguration;
  }

  @Override
  public void saveConfig() throws IOException {
    if (this.fileConfiguration != null && this.config != null) {
      this.getConfig().save(this.config.toFile());
    }
  }

  @Override
  public void saveDefaultConfig() {
    if (!Files.exists(this.config)) {
      this.loader.saveResource(this.name, false);
    }
  }

  @Override
  public void read() throws IOException {
    this.saveDefaultConfig();
    this.getConfig();
  }

  @Override
  public @NotNull DeluxeMediaPlugin getPlugin() {
    return this.plugin;
  }

  @Override
  public @NotNull String getFileName() {
    return this.name;
  }

  @Override
  public @NotNull Path getConfigFile() {
    return this.config;
  }

  @Override
  public @NotNull FileConfiguration getFileConfiguration() {
    return this.fileConfiguration;
  }
}
