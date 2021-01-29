package com.github.pulsebeat02.video.player;

import com.github.pulsebeat02.MinecraftMediaLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractVideoPlayer {

    private final MinecraftMediaLibrary library;
    private final String url;
    private int width;
    private int height;
    private final Consumer<int[]> callback;

    public AbstractVideoPlayer(@NotNull final MinecraftMediaLibrary library,
                               @NotNull final String url,
                               final int width, final int height,
                               @NotNull final Consumer<int[]> callback) {
        this.library = library;
        this.url = url;
        this.width = width;
        this.height = height;
        this.callback = callback;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public Consumer<int[]> getCallback() {
        return callback;
    }

    abstract public void start();

    abstract public void stop();

}
