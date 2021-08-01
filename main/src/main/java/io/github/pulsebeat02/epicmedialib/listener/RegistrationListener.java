package io.github.pulsebeat02.epicmedialib.listener;

import io.github.pulsebeat02.epicmedialib.Logger;
import io.github.pulsebeat02.epicmedialib.MediaLibraryCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class RegistrationListener implements Listener {

  private final MediaLibraryCore core;

  public RegistrationListener(@NotNull final MediaLibraryCore core) {
    this.core = core;
    final Plugin plugin = core.getPlugin();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  protected void onPlayerJoin(final PlayerJoinEvent event) {
    final Player p = event.getPlayer();
    core.getHandler().registerPlayer(p);
    Logger.info(String.format("Registered Player %s", p.getUniqueId()));
  }

  /**
   * Unregisters the player on leave.
   *
   * @param event PlayerQuitEvent event
   */
  @EventHandler
  protected void onPlayerLeave(final PlayerQuitEvent event) {
    final Player p = event.getPlayer();
    core.getHandler().unregisterPlayer(p);
    Logger.info(String.format("Unregistered Player %s", p.getUniqueId()));
  }
}
