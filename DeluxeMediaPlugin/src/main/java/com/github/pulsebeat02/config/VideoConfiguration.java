package com.github.pulsebeat02.config;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.MinecraftMediaLibrary;
import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.video.dither.AbstractDitherHolder;
import com.github.pulsebeat02.video.dither.DitherSetting;
import com.github.pulsebeat02.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.video.player.AbstractVideoPlayer;
import com.github.pulsebeat02.video.player.BasicVideoPlayer;
import com.github.pulsebeat02.video.player.VLCJIntegratedPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class VideoConfiguration extends AbstractConfiguration {

    private AbstractVideoPlayer player;
    private ItemFrameCallback callback;

    public VideoConfiguration(@NotNull final DeluxeMediaPlugin plugin, final @NotNull String name) {
        super(plugin, name);
    }

    @Override
    public void deserialize() {
        FileConfiguration configuration = getFileConfiguration();
        configuration.set("enabled", player != null);
        configuration.set("url", player.getUrl());
        configuration.set("video-width", player.getWidth());
        configuration.set("video-height", player.getHeight());
        configuration.set("itemframe-width", callback.getWidth());
        configuration.set("itemframe-height", callback.getHeight());
        configuration.set("starting-map-id", callback.getMap());
        configuration.set("dither-setting", callback.getType().getSetting().name());
    }

    @Override
    public void serialize() {
        FileConfiguration configuration = getFileConfiguration();
        boolean enabled = configuration.getBoolean("enabled");
        String url = configuration.getString("url");
        int width = configuration.getInt("video-width");
        int height = configuration.getInt("video-height");
        int frameWidth = configuration.getInt("itemframe-width");
        int frameHeight = configuration.getInt("itemframe-height");
        int startingMapID = configuration.getInt("starting-map-id");
        String ditherSetting = configuration.getString("dither-setting");
        AbstractDitherHolder holder = null;
        for (DitherSetting setting : DitherSetting.values()) {
            if (setting.name().equalsIgnoreCase(ditherSetting)) {
                holder = setting.getHolder();
                break;
            }
        }
        if (holder == null) {
            Logger.error("Setting " + ditherSetting + " is NOT a valid setting!");
        }
        boolean vlcj = configuration.getBoolean("using-vlcj");
        if (enabled) {
            MinecraftMediaLibrary library = player.getLibrary();
            ItemFrameCallback callback = new ItemFrameCallback(getPlugin().getLibrary(), null, startingMapID, frameWidth, frameHeight, width, 0, holder);
            if (vlcj) {
                player = new VLCJIntegratedPlayer(library, url, width, height, callback::send);
            } else {
                player = new BasicVideoPlayer(library, url, width, height, callback::send);
            }
            this.callback = callback;
        }
    }

}
