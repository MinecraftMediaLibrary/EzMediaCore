/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

public final class ForcefulResourcepackListener implements Listener {

  private final MediaLibraryCore core;
  private final Set<UUID> uuids;
  private final String url;
  private final byte[] hash;

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
  public void onResourcepackStatus(@NotNull final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    if (!this.uuids.contains(uuid)) {
      return;
    }
    switch (event.getStatus()) {
      case FAILED_DOWNLOAD -> player.setResourcePack(this.url, this.hash);
      case DECLINED, SUCCESSFULLY_LOADED -> {
        this.uuids.remove(uuid);
        if (this.uuids.isEmpty()) {
          PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
        }
      }
      default -> throw new IllegalArgumentException("Invalid resourcepack status!");
    }
  }

  public MediaLibraryCore getCore() {
    return this.core;
  }

  public Set<UUID> getUuids() {
    return this.uuids;
  }

  public String getUrl() {
    return this.url;
  }

  public byte[] getHash() {
    return this.hash;
  }
}
