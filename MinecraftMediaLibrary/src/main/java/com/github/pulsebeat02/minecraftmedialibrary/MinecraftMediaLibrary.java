package com.github.pulsebeat02.minecraftmedialibrary;

import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.JaveDependencyHandler;
import com.github.pulsebeat02.minecraftmedialibrary.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.NMSReflectionManager;
import com.github.pulsebeat02.minecraftmedialibrary.reflection.TinyProtocol;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.util.concurrent.CompletableFuture;

public class MinecraftMediaLibrary {

    private final Plugin plugin;
    private final TinyProtocol protocol;
    private final String parent;
    private final PacketHandler handler;
    private final boolean vlcj;

    private final PlayerJoinLeaveHandler listener;

    public MinecraftMediaLibrary(@NotNull final Plugin plugin,
                                 @NotNull final String path,
                                 final boolean isUsingVLCJ) {
        this.plugin = plugin;
        this.protocol = new TinyProtocol(plugin) {
            @Override
            public Object onPacketOutAsync(final Player player, final Channel channel, final Object packet) {
                return handler.onPacketInterceptOut(player, packet);
            }

            @Override
            public Object onPacketInAsync(final Player player, final Channel channel, final Object packet) {
                return handler.onPacketInterceptIn(player, packet);
            }
        };
        this.handler = NMSReflectionManager.getNewPacketHandlerInstance();
        this.parent = path;
        this.vlcj = isUsingVLCJ;
        this.listener = new PlayerJoinLeaveHandler(this);
        CompletableFuture.runAsync(this::asyncTasks);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        Logger.info("Plugin " + plugin.getName() + " initialized MinecraftMediaLibrary");
        Logger.info("=====================================");
        Logger.info("Path: " + path);
        Logger.info("Using VLCJ? " + (isUsingVLCJ ? "Yes" : "No"));
        Logger.info("=====================================");
        checkJavaVersion();
    }

    private void asyncTasks() {
        new DependencyManagement().installAndLoad();
        new JaveDependencyHandler().installDependency();
        if (vlcj) {
            new MediaPlayerFactory();
        }
    }

    private void checkJavaVersion() {
        final String[] verison = System.getProperty("java.version").split("\\.");
        final int major = Integer.parseInt(verison[1]);
        if (major < 11) {
            Logger.warn("MinecraftMediaPlugin is moving towards a newer Java Version (Java 11) \n" +
                    "Please switch as soon as possible before the library will be incompatible \n" +
                    "with your server. If you want to read more information surrounding this, \n" +
                    "you may want to take a look here at " +
                    "https://papermc.io/forums/t/java-11-mc-1-17-and-paper/5615");
        }
    }

    public void shutdown() {
        Logger.info("Shutting Down!");
        HandlerList.unregisterAll(listener);
        Logger.info("Good Bye");
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
