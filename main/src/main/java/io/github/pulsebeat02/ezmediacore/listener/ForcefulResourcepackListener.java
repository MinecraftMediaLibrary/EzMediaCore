package io.github.pulsebeat02.ezmediacore.listener;

import io.github.pulsebeat02.ezmediacore.Logger;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class ForcefulResourcepackListener implements Listener {

  private final Set<UUID> uuids;
  private final String url;
  private final byte[] hash;

  public ForcefulResourcepackListener(
      @NotNull final Plugin plugin,
      @NotNull final Set<UUID> uuids,
      @NotNull final String url,
      final byte @NotNull [] hash) {
    this.uuids = uuids;
    this.url = url;
    this.hash = hash;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    sendResourcepack();
  }

  private void sendResourcepack() {
    for (final UUID uuid : this.uuids) {
      final Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
        player.setResourcePack(this.url, this.hash);
      } else {
        Logger.info(String.format("Could not set the resourcepack for %s! (%s)", uuid, this.url));
      }
    }
  }

  public void start(@NotNull final Plugin plugin) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!ForcefulResourcepackListener.this.uuids.isEmpty()) {
          Logger.info(
              String.format(
                  "Could not force all players to load resourcepack! (%s)",
                  ForcefulResourcepackListener.this.uuids));
          PlayerResourcePackStatusEvent.getHandlerList().unregister(plugin);
        }
      }
    }.runTaskLater(plugin, 6000L);
  }

  @EventHandler
  public void onResourcepackStatus(final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    if (!this.uuids.contains(uuid)) {
      return;
    }
    switch (event.getStatus()) {
      case FAILED_DOWNLOAD:
        player.setResourcePack(this.url, this.hash);
        break;
      case DECLINED:
      case SUCCESSFULLY_LOADED:
      case ACCEPTED:
        this.uuids.remove(uuid);
        if (this.uuids.isEmpty()) {
          PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
        }
        break;
    }
  }
}
