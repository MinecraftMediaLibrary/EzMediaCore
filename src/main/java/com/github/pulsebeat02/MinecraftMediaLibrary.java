package com.github.pulsebeat02;

import com.github.pulsebeat02.dependency.DependencyManagement;
import com.github.pulsebeat02.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.nms.PacketHandler;
import com.github.pulsebeat02.reflection.NMSReflectionManager;
import com.github.pulsebeat02.reflection.TinyProtocol;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

public class MinecraftMediaLibrary {

    private Plugin plugin;
    private TinyProtocol protocol;
    private String parent;
    private PacketHandler handler;
    private boolean vlcj;

    public MinecraftMediaLibrary(@NotNull final Plugin plugin,
                                 @NotNull final String path,
                                 final boolean isUsingVLCJ) {
        new Thread(() -> {
            this.plugin = plugin;
            this.protocol = new TinyProtocol(plugin) {
                @Override
                public Object onPacketOutAsync(Player player, Channel channel, Object packet) {
                    return handler.onPacketInterceptOut(player, packet);
                }

                @Override
                public Object onPacketInAsync(Player player, Channel channel, Object packet) {
                    return handler.onPacketInterceptIn(player, packet);
                }
            };
            this.handler = NMSReflectionManager.getNewPacketHandlerInstance();
            this.parent = path;
            this.vlcj = isUsingVLCJ;
            new DependencyManagement().installAndLoad();
            if (isUsingVLCJ) {
                new MediaPlayerFactory();
            }
            Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveHandler(this), plugin);
            Logger.info("Plugin " + plugin.getName() + " initialized MinecraftMediaLibrary");
            Logger.info("=====================================");
            Logger.info("Path: " + path);
            Logger.info("Using VLCJ? " + (isUsingVLCJ ? "Yes" : "No"));
            Logger.info("=====================================");
        });
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public TinyProtocol getProtocol() {
        return protocol;
    }

    public String getPath() {
        return parent;
    }

    public boolean isUsingVLCJ() {
        return vlcj;
    }

    public String getParent() {
        return parent;
    }

    public boolean isVlcj() {
        return vlcj;
    }

}
