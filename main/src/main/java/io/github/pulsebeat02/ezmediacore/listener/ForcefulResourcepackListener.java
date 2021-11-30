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

import static java.util.Objects.requireNonNull;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public record ForcefulResourcepackListener(MediaLibraryCore core,
                                           Set<UUID> uuids,
                                           String url, byte[] hash) implements Listener {

  public ForcefulResourcepackListener(
      @NotNull final MediaLibraryCore core,
      @NotNull final Set<UUID> uuids,
      @NotNull final String url,
      final byte @NotNull [] hash) {
    this.core = core;
    this.uuids = uuids;
    this.url = url;
    this.hash = hash;
    this.register();
    this.sendResourcepack();
  }

  private void register() {
    final Plugin plugin = this.core.getPlugin();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  private void sendResourcepack() {
    for (final UUID uuid : this.uuids) {
      requireNonNull(this.core.getPlugin().getServer().getPlayer(uuid), "Player %s is not online!".formatted(uuid))
          .setResourcePack(this.url, this.hash);
    }
  }

  @EventHandler
  public void onResourcepackStatus(@NotNull final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    if (!this.isIncluded(player)) {
      return;
    }
    this.handleResourcepackStatus(event.getStatus(), player);
  }

  private boolean isIncluded(@NotNull final Player player) {
    return this.uuids.contains(player.getUniqueId());
  }

  private void handleResourcepackStatus(
      @NotNull final PlayerResourcePackStatusEvent.Status status,
      @NotNull final Player player) {
    switch (status) {
      case FAILED_DOWNLOAD -> this.failed(player);
      case ACCEPTED, DECLINED, SUCCESSFULLY_LOADED -> this.successful(player);
      default -> throw new IllegalArgumentException("Invalid resourcepack status!");
    }
  }

  private void successful(@NotNull final Player player) {
    this.uuids.remove(player.getUniqueId());
    this.unregister();
  }

  private void unregister() {
    if (this.uuids.isEmpty()) {
      PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
    }
  }

  private void failed(@NotNull final Player player) {
    player.setResourcePack(this.url, this.hash);
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
