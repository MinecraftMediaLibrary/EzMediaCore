package com.github.pulsebeat02.video.itemframe;

public interface AbstractCallback {

    /**
     *
     * Sends data for map packets to the players.
     *
     * @param data to send
     */
    void send(final int[] data);

}
