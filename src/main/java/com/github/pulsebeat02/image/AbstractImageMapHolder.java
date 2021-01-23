package com.github.pulsebeat02.image;

public interface AbstractImageMapHolder {

    /**
     * Draws a specified image onto a Map.
     */
    void drawImage();

    /**
     * Called before image is drawn.
     */
    void onDrawImage();

}
