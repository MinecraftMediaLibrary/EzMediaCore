package com.github.pulsebeat02;

import com.github.pulsebeat02.listener.PlayerJoinLeaveHandler;
import com.github.pulsebeat02.nms.PacketHandler;
import com.github.pulsebeat02.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

import java.nio.file.Path;

public class MinecraftMediaLibrary {

    private PacketHandler handler;
    private TinyProtocol protocol;
    private Path parent;
    private boolean vlcj;

    public MinecraftMediaLibrary(final Plugin plugin, final Path path, final boolean isUsingVLCJ) {
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
        this.parent = path;
        this.vlcj = isUsingVLCJ;
        if (isUsingVLCJ) {
            new MediaPlayerFactory();
        }
        Bukkit.getPluginManager().registerEvents(new PlayerJoinLeaveHandler(this), plugin);
    }

    public PacketHandler getHandler() {
        return handler;
    }

}
