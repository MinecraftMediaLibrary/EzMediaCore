package com.github.pulsebeat02.image;

import com.github.pulsebeat02.Logger;
import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.utility.VideoUtilities;
import com.github.pulsebeat02.video.dither.JetpImageDither;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public class ImageMap implements AbstractImageMapHolder {

    private final MinecraftMediaLibrary library;
    private final UUID[] viewers;
    private final int map;
    private final File image;
    private final int height;
    private final int width;

    public ImageMap(@NotNull final MinecraftMediaLibrary library,
                    @NotNull final UUID[] viewers,
                    final int map,
                    @NotNull final File image,
                    final int height,
                    final int width) {
        this.library = library;
        this.viewers = viewers;
        this.map = map;
        this.image = image;
        this.height = height;
        this.width = width;
        Logger.info("Initialized Image at Map ID " + map + " (Source: " + image.getAbsolutePath() + ")");
    }

    @Override
    public void drawImage() {
        ByteBuffer buffer = JetpImageDither.ditherIntoMinecraft(Objects.requireNonNull(VideoUtilities.getBuffer(image)), width);
        library.getHandler().display(viewers, map, width, height, buffer, width);
        Logger.info("Drew Image at Map ID " + map + " (Source: " + image.getAbsolutePath() + ")");
    }

    @Override
    public void onDrawImage() {
    }

    public static class Builder {

        private UUID[] viewers;
        private int map;
        private File image;
        private int height;
        private int width;

        public Builder setViewers(UUID[] viewers) {
            this.viewers = viewers;
            return this;
        }

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

        public ImageMap createImageMap(final MinecraftMediaLibrary library) {
            return new ImageMap(library, viewers, map, image, height, width);
        }

    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

    public UUID[] getViewers() {
        return viewers;
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
