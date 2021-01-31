package com.github.pulsebeat02.config;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.image.MapImage;
import com.github.pulsebeat02.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PictureConfiguration extends AbstractConfiguration {

    private final Set<MapImage> images;

    public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
        super(plugin, "pictures.yml");
        this.images = new HashSet<>();
    }

    public void addPhoto(final int map, @NotNull final File file,
                         final int width, final int height) {
        images.add(new MapImage(getPlugin().getLibrary(), map, file, width, height));
    }

    @Override
    public void deserialize() {
        FileConfiguration configuration = getFileConfiguration();
        for (MapImage image : images) {
            long key = image.getMap();
            configuration.set(key + ".location", image.getImage().getAbsolutePath());
            configuration.set(key + ".width", image.getWidth());
            configuration.set(key + ".height", image.getHeight());
        }
        saveConfig();
    }

    @Override
    public void serialize() {
        FileConfiguration configuration = getFileConfiguration();
        for (String key :  configuration.getKeys(false)) {
            long id = Long.parseLong(key);
            File file = new File(Objects.requireNonNull(configuration.getString(id + ".location")));
            if (!file.exists()) {
                Logger.error("Could not read " + file.getAbsolutePath() + " at id " + id + "!");
                continue;
            }
            int width = configuration.getInt(id + "width");
            int height = configuration.getInt(id + "height");
            images.add(new MapImage(getPlugin().getLibrary(), id, file, width, height));
        }
    }

}
