package com.github.pulsebeat02.deluxemediaplugin;

import com.github.pulsebeat02.deluxemediaplugin.command.DitherCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.ImageCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.VideoCommand;
import com.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.PictureConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.VideoConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.utility.CommandUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DeluxeMediaPlugin extends JavaPlugin {

    public static boolean OUTDATED = false;

    private MinecraftMediaLibrary library;
    private Logger logger;

    private PluginCommand dither;
    private PluginCommand video;
    private PluginCommand image;

    private HttpConfiguration httpConfiguration;
    private PictureConfiguration pictureConfiguration;
    private VideoConfiguration videoConfiguration;

    @Override
    public void onEnable() {
        logger = getLogger();
        if (!OUTDATED) {
            com.github.pulsebeat02.minecraftmedialibrary.logger.Logger.setVerbose(true);
            logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Initializing");
            logger.info(ChatColor.GOLD + "Loading MinecraftMediaLibrary Instance...");
            library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
            registerCommands();
            registerConfigurations();
            final Metrics metrics = new Metrics(this, 10229);
            logger.info(ChatColor.GOLD + "Finished Loading Instance and Plugin");
        } else {
            logger.severe("Plugin cannot load until server version is at least 1.8");
        }
    }

    @Override
    public void onDisable() {
        logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Shutting Down");
        logger.info(ChatColor.GOLD + "Shutting Down MinecraftMediaLibrary Instance...");
        if (library != null) {
            library.shutdown();
        } else {
            logger.severe("WARNING: MinecraftMediaLibrary instance is null... something is fishy going on.");
        }
        CommandUtilities.unRegisterBukkitCommand(this, dither);
        CommandUtilities.unRegisterBukkitCommand(this, video);
        CommandUtilities.unRegisterBukkitCommand(this, image);
        logger.info(ChatColor.GOLD + "Enclosing MinecraftMediaLibrary and Plugin Successfully Shutdown");
    }

    private void registerConfigurations() {

        httpConfiguration = new HttpConfiguration(this);
        pictureConfiguration = new PictureConfiguration(this);
        videoConfiguration = new VideoConfiguration(this);

        httpConfiguration.serialize();
        pictureConfiguration.serialize();
        videoConfiguration.serialize();

    }

    private void registerCommands() {

        dither = getCommand("dither");
        video = getCommand("video");
        image = getCommand("image");

        final DitherCommand ditherCommand = new DitherCommand(this);
        final ImageCommand imageCommand = new ImageCommand(this);
        final VideoCommand videoCommand = new VideoCommand(this);

        dither.setExecutor(ditherCommand);
        dither.setTabCompleter(ditherCommand);

        video.setExecutor(videoCommand);
        video.setTabCompleter(videoCommand);

        image.setExecutor(imageCommand);
        image.setTabCompleter(imageCommand);

    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
    }

    public PluginCommand getDither() {
        return dither;
    }

    public PluginCommand getVideo() {
        return video;
    }

    public PluginCommand getImage() {
        return image;
    }

    public HttpConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    public PictureConfiguration getPictureConfiguration() {
        return pictureConfiguration;
    }

    public VideoConfiguration getVideoConfiguration() {
        return videoConfiguration;
    }

}
