/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin;

import com.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import com.github.pulsebeat02.deluxemediaplugin.command.CommandHandler;
import com.github.pulsebeat02.deluxemediaplugin.config.EncoderConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.HttpConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.PictureConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.config.VideoConfiguration;
import com.github.pulsebeat02.deluxemediaplugin.update.PluginUpdateChecker;
import com.github.pulsebeat02.deluxemediaplugin.utility.CommandUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.api.ChatColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class DeluxeMediaPlugin extends JavaPlugin {

    public static boolean OUTDATED = false;

    private MinecraftMediaLibrary library;
    private BukkitAudiences audiences;
    private CommandHandler handler;
    private Logger logger;
    private HttpConfiguration httpConfiguration;
    private PictureConfiguration pictureConfiguration;
    private VideoConfiguration videoConfiguration;
    private EncoderConfiguration encoderConfiguration;

    @Override
    public void onEnable() {
        logger = getLogger();
        if (!OUTDATED) {
            CommandUtilities.ensureInit();
            com.github.pulsebeat02.minecraftmedialibrary.logger.Logger.setVerbose(true);
            logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Initializing");
            logger.info(ChatColor.GOLD + "Loading MinecraftMediaLibrary Instance...");
            library = new MinecraftMediaLibrary(this, getDataFolder().getPath(), true);
            registerCommands();
            registerConfigurations();
            checkUpdates();
            audiences = BukkitAudiences.create(this);
            logger.info(ChatColor.GOLD + "Finished Loading Instance and Plugin");
        } else {
            logger.severe("Plugin cannot load until server version is at least 1.8");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        logger.info(ChatColor.GOLD + "DeluxeMediaPlugin is Shutting Down");
        logger.info(ChatColor.GOLD + "Shutting Down MinecraftMediaLibrary Instance...");
        if (library != null) {
            library.shutdown();
        } else {
            logger.severe(
                    "WARNING: MinecraftMediaLibrary instance is null... something is fishy going on.");
        }
        for (BaseCommand cmd : handler.getCommands()) {
            CommandUtilities.unRegisterBukkitCommand(this, cmd);
        }

        logger.info(
                ChatColor.GOLD + "Enclosing MinecraftMediaLibrary and Plugin Successfully Shutdown");
    }

    private void registerConfigurations() {

        httpConfiguration = new HttpConfiguration(this);
        pictureConfiguration = new PictureConfiguration(this);
        videoConfiguration = new VideoConfiguration(this);
        encoderConfiguration = new EncoderConfiguration(this);

        httpConfiguration.read();
        pictureConfiguration.read();
        videoConfiguration.read();
        encoderConfiguration.read();
    }

    private void registerCommands() {
        handler = new CommandHandler(this);
    }

    private void checkUpdates() {
        final Metrics metrics = new Metrics(this, 10229);
        new PluginUpdateChecker(this).checkForUpdates();
    }

    public MinecraftMediaLibrary getLibrary() {
        return library;
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

    public EncoderConfiguration getEncoderConfiguration() {
        return encoderConfiguration;
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public CommandHandler getHandler() {
        return handler;
    }
}
