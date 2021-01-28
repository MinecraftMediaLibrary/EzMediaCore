package com.github.pulsebeat02.image;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.utility.FileUtilities;
import com.github.pulsebeat02.video.dither.FloydImageDither;
import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.utility.VideoUtilities;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapImage implements AbstractImageMapHolder, ConfigurationSerializable {

    private final MinecraftMediaLibrary library;
    private final int map;
    private final File image;
    private final int height;
    private final int width;

    public MapImage(@NotNull final MinecraftMediaLibrary library,
                    final int map,
                    @NotNull final File image,
                    final int width,
                    final int height) {
        this.library = library;
        this.map = map;
        this.image = image;
        this.width = width;
        this.height = height;
        Logger.info("Initialized Image at Map ID " + map + " (Source: " + image.getAbsolutePath() + ")");
    }

    public MapImage(@NotNull final MinecraftMediaLibrary library,
                    final int map,
                    @NotNull final String url,
                    final int width,
                    final int height) {
        this.library = library;
        this.map = map;
        this.image = FileUtilities.downloadImageFile(url, library.getPath());
        this.width = width;
        this.height = height;
        Logger.info("Initialized Image at Map ID " + map + " (Source: " + image.getAbsolutePath() + ")");
    }

    @Override
    public void drawImage() {
        onDrawImage();
        ByteBuffer buffer = new FloydImageDither().ditherIntoMinecraft(Objects.requireNonNull(VideoUtilities.getBuffer(image)), width);
        library.getHandler().display(null, map, width, height, buffer, width);
        Logger.info("Drew Image at Map ID " + map + " (Source: " + image.getAbsolutePath() + ")");
    }

    @Override
    public void onDrawImage() {
    }

    public static class Builder {

        private int map;
        private File image;
        private int height;
        private int width;

        public Builder setMap(int map) {
            this.map = map;
            return this;
        }

        public Builder setImage(File image) {
            this.image = image;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public MapImage createImageMap(final MinecraftMediaLibrary library) {
            return new MapImage(library, map, image, height, width);
        }

    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("map", map);
        serialized.put("image", image.getAbsolutePath());
        serialized.put("width", width);
        serialized.put("height", height);
        return serialized;
    }

    public static MapImage deserialize(@NotNull final MinecraftMediaLibrary library, @NotNull final Map<String, Object> deserialize) {
        return new MapImage(library,
                NumberConversions.toInt(deserialize.get("map")),
                new File(String.valueOf(deserialize.get("image"))),
                NumberConversions.toInt(deserialize.get("width")), NumberConversions.toInt(deserialize.get("height")));
    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

    public int getMap() {
        return map;
    }

    public File getImage() {
        return image;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

}
