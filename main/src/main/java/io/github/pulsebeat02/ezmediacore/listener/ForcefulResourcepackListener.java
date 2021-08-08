package io.github.pulsebeat02.ezmediacore.listener;

import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public record ForcefulResourcepackListener(@NotNull MediaLibraryCore core,
                                           Set<UUID> uuids, String url,
                                           byte[] hash) implements Listener {

  public ForcefulResourcepackListener(
      @NotNull final MediaLibraryCore core,
      @NotNull final Set<UUID> uuids,
      @NotNull final String url,
      final byte @NotNull [] hash) {
    this.core = core;
    this.uuids = uuids;
    this.url = url;
    this.hash = hash;
    final Plugin plugin = core.getPlugin();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.sendResourcepack();
  }

  private void sendResourcepack() {
    for (final UUID uuid : this.uuids) {
      final Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
        player.setResourcePack(this.url, this.hash);
      } else {
        Logger.info("Could not set the resourcepack for %s! (%s)".formatted(uuid, this.url));
      }
    }
  }

  public void start() {
    ExecutorProvider.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
      if (!ForcefulResourcepackListener.this.uuids.isEmpty()) {
        Logger.info(
            "Could not force all players to load resourcepack! (%s)".formatted(
                ForcefulResourcepackListener.this.uuids));
        PlayerResourcePackStatusEvent.getHandlerList().unregister(this.core.getPlugin());
      }
    }, 5, TimeUnit.MINUTES);
  }

  @EventHandler
  public void onResourcepackStatus(final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    if (!this.uuids.contains(uuid)) {
      return;
    }
    switch (event.getStatus()) {
      case FAILED_DOWNLOAD -> player.setResourcePack(this.url, this.hash);
      case DECLINED, SUCCESSFULLY_LOADED, ACCEPTED -> {
        this.uuids.remove(uuid);
        if (this.uuids.isEmpty()) {
          PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
        }
      }
      default -> throw new IllegalArgumentException("Invalid resourcepack status!");
    }
  }
}
