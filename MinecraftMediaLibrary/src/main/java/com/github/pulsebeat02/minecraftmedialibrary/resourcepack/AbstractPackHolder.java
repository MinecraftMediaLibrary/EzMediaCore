package com.github.pulsebeat02.minecraftmedialibrary.resourcepack;

public interface AbstractPackHolder {

    /**
     * Builds the resourcepack with all the files
     * and specified pack.mcmeta values.
     */
    void buildResourcePack();

    /**
     * Called before the resourcepack
     * starts building.
     */
    void onResourcepackBuild();

}
