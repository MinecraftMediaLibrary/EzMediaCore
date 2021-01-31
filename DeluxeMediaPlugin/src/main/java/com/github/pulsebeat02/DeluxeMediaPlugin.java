package com.github.pulsebeat02;

import org.bukkit.plugin.java.JavaPlugin;

public class DeluxeMediaPlugin extends JavaPlugin {

    private MinecraftMediaLibrary library;

    @Override
    public void onEnable() {
        library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

}
