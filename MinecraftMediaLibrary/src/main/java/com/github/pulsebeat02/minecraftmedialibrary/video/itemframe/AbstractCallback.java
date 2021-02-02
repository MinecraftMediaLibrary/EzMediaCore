package com.github.pulsebeat02.minecraftmedialibrary.video.itemframe;

import org.jetbrains.annotations.NotNull;

public interface AbstractCallback {

    /**
     * Sends data for map packets to the players.
     *
     * @param data to send
     */
    void send(@NotNull final int[] data);

}
