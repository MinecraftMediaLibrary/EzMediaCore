package com.github.pulsebeat02.listener;

import com.github.pulsebeat02.MinecraftMediaLibrary;
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
        library.getHandler().registerPlayer(event.getPlayer());
    }

    @EventHandler
    private void onEvent(final PlayerQuitEvent event) {
        library.getHandler().unregisterPlayer(event.getPlayer());
    }

}
