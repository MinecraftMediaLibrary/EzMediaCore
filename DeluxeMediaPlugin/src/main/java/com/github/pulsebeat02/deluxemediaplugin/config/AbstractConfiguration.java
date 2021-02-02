package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
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

    public AbstractConfiguration(@NotNull final DeluxeMediaPlugin plugin, @NotNull final String name) {
        this.plugin = plugin;
        this.fileName = name;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            fileConfiguration.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    public void saveConfig() {
        if (fileConfiguration != null && configFile != null) {
            try {
                getConfig().save(configFile);
            } catch (final IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
            }
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
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
