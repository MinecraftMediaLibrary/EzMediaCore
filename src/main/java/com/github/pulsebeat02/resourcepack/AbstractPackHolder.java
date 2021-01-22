package com.github.pulsebeat02.resourcepack;

import java.io.IOException;

public interface AbstractPackHolder {

    /**
     *
     * Builds the resourcepack with all the files
     * and specified pack.mcmeta values.
     *
     * @throws IOException if build fails
     */
    void buildResourcePack() throws IOException;

    /**
     *
     * Called before the resourcepack
     * starts building.
     *
     */
    void onResourcepackBuild();

}
