/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
package rewrite.listener;

import rewrite.EzMediaCore;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import rewrite.reflect.PacketToolsProvider;


public final class RegistrationListener implements Listener{

  private final EzMediaCore core;

  public RegistrationListener(final EzMediaCore core) {
    this.core = core;
    this.registerListener();
  }

  private void registerListener() {
    final Plugin plugin = this.core.getPlugin();
    final Server server = plugin.getServer();
    final PluginManager manager = server.getPluginManager();
    manager.registerEvents(this, plugin);
  }

  @EventHandler
  public void onPlayerJoin( final PlayerJoinEvent event) {
    final Player p = event.getPlayer();
    final PacketHandler handler = PacketToolsProvider.getPacketHandler();
    handler.injectPlayer(p);
  }

  @EventHandler
  public void onPlayerLeave( final PlayerQuitEvent event) {
    final Player p = event.getPlayer();
    final PacketHandler handler = PacketToolsProvider.getPacketHandler();
    handler.uninjectPlayer(p);
  }

  public EzMediaCore getCore() {
    return this.core;
  }
}
