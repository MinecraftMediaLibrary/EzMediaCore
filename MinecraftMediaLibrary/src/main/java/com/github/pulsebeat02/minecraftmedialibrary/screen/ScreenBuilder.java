package com.github.pulsebeat02.minecraftmedialibrary.screen;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ScreenBuilder {

    private final Location location;

    public ScreenBuilder(@NotNull final Player player) {
        location = player.getLocation();
    }

    // TODO: Implement map build helper class
    public void buildMapScreen(final int width, final int height, final int startingMap) {
        final int maps = width * height;
        for (int i = 0; i < maps; i++) {
            final int row = i % width;
        }
    }

}
