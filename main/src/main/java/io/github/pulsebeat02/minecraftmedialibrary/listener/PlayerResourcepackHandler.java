/*.........................................................................................
. Copyright © 2021 Brandon Li
.                                                                                        .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this
. software and associated documentation files (the “Software”), to deal in the Software
. without restriction, including without limitation the rights to use, copy, modify, merge,
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit
. persons to whom the Software is furnished to do so, subject to the following conditions:
.
. The above copyright notice and this permission notice shall be included in all copies
. or substantial portions of the Software.
.
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
. EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
. MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
. NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
. BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
. ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
. CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
.  SOFTWARE.
.                                                                                        .
.........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.listener;

import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/** Simple class for making sure the client downloads a resourcepack. */
public class PlayerResourcepackHandler implements Listener {

  private final Set<UUID> uuids;
  private final String url;
  private final byte[] hash;

  /**
   * Instantiates a PlayerResourcepackHandler.
   *
   * @param plugin the plugin
   * @param uuids the player
   * @param url the url
   * @param hash the hash
   */
  public PlayerResourcepackHandler(
      @NotNull final Plugin plugin,
      @NotNull final Set<UUID> uuids,
      @NotNull final String url,
      final byte @NotNull [] hash) {
    this.uuids = uuids;
    this.url = url;
    this.hash = hash;
    initialize(plugin);
  }

  private void initialize(@NotNull final Plugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    for (final UUID uuid : uuids) {
      final Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
        player.setResourcePack(url, hash);
      } else {
        Logger.info(String.format("Could not set the resourcepack for %s! (%s)", uuid, url));
      }
    }
  }

  public void start(@NotNull final Plugin plugin) {
    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () -> {
              if (!uuids.isEmpty()) {
                Logger.info(
                    String.format("Could not force all players to load resourcepack! (%s)", uuids));
                PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
              }
            },
            6000L);
  }

  @EventHandler
  public void onResourcepackStatus(final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    final UUID uuid = player.getUniqueId();
    if (!uuids.contains(uuid)) {
      return;
    }
    switch (event.getStatus()) {
      case FAILED_DOWNLOAD:
        player.setResourcePack(url, hash);
        break;
      case DECLINED:
      case SUCCESSFULLY_LOADED:
      case ACCEPTED:
        uuids.remove(uuid);
        if (uuids.isEmpty()) {
          PlayerResourcePackStatusEvent.getHandlerList().unregister(this);
        }
        break;
    }
  }
}
