package com.github.pulsebeat02;

import com.github.pulsebeat02.command.DitherCommand;
import com.github.pulsebeat02.command.ImageCommand;
import com.github.pulsebeat02.command.VideoCommand;
import com.github.pulsebeat02.utility.CommandUtilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DeluxeMediaPlugin extends JavaPlugin {

    private MinecraftMediaLibrary library;
    private Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Initializing");
        logger.info(ChatColor.GOLD + "Loading MinecraftMediaLibrary Instance...");
        library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
        registerCommands();
        logger.info(ChatColor.GOLD + "Finished Loading Instance and Plugin");
    }

    private PluginCommand dither;
    private PluginCommand video;
    private PluginCommand image;

    @Override
    public void onDisable() {
        logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Shutting Down");
        logger.info(ChatColor.GOLD + "Shutting Down MinecraftMediaLibrary Instance...");
        library.shutdown();
        CommandUtilities.unRegisterBukkitCommand(this, dither);
        CommandUtilities.unRegisterBukkitCommand(this, video);
        CommandUtilities.unRegisterBukkitCommand(this, image);
        logger.info(ChatColor.GOLD + "Enclosing MinecraftMediaLibrary and Plugin Successfully Shutdown");
    }

    public void registerCommands() {

        dither = getCommand("dither");
        video = getCommand("video");
        image = getCommand("image");

        DitherCommand ditherCommand = new DitherCommand(this);
        ImageCommand imageCommand = new ImageCommand(this);
        VideoCommand videoCommand = new VideoCommand(this);

        if (dither != null) {
            dither.setExecutor(ditherCommand);
            dither.setTabCompleter(ditherCommand);
        }

        if (video != null) {
            video.setExecutor(videoCommand);
            video.setTabCompleter(videoCommand);
        }

        if (image != null) {
            image.setExecutor(imageCommand);
            image.setTabCompleter(imageCommand);
        }

    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

}
