package com.github.pulsebeat02.listener;

import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.MinecraftMediaLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveHandler implements Listener {

    private final MinecraftMediaLibrary library;

    public PlayerJoinLeaveHandler(final MinecraftMediaLibrary library) {
        this.library = library;
    }

    @EventHandler
    private void onEvent(final PlayerJoinEvent event) {
        Player p = event.getPlayer();
        library.getHandler().registerPlayer(p);
        Logger.info("Registered Player " + p.getUniqueId());
    }

    @EventHandler
    private void onEvent(final PlayerQuitEvent event) {
        Player p = event.getPlayer();
        library.getHandler().unregisterPlayer(p);
        Logger.info("Unregistered Player " + p.getUniqueId());
    }

}
