package com.github.pulsebeat02.config;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.image.MapImage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class PictureConfiguration {

    private final DeluxeMediaPlugin plugin;
    private final String fileName;

    private final File configFile;
    private FileConfiguration fileConfiguration;

    private final Set<MapImage> images;

    public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
        this.plugin = plugin;
        this.fileName = "pictures.yml";
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.images = new HashSet<>();
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
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
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
            }
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    public void addPhoto(final int map, @NotNull final File file,
                         final int width, final int height) {

    }

}
