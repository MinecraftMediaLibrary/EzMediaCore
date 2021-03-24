/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.listener;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The specified PlayerJoinHandler listener that is used under the library. It is used to register
 * the correct listener. The listener is registered under the Plugin that was passed into the
 * library on initialization.
 */
public class PlayerJoinLeaveHandler implements Listener {

  private final MinecraftMediaLibrary library;

  /**
   * Instantiates a new PlayerJoinLeaveHandler.
   *
   * @param library the library
   */
  public PlayerJoinLeaveHandler(final MinecraftMediaLibrary library) {
    this.library = library;
  }

  /**
   * Registers the player on join.
   *
   * @param event PlayerJoinEvent event
   */
  @EventHandler
  protected void onPlayerJoin(final PlayerJoinEvent event) {
    final Player p = event.getPlayer();
    library.getHandler().registerPlayer(p);
    Logger.info("Registered Player " + p.getUniqueId());
  }

  /**
   * Unregisters the player on leave.
   *
   * @param event PlayerQuitEvent event
   */
  @EventHandler
  protected void onPlayerLeave(final PlayerQuitEvent event) {
    final Player p = event.getPlayer();
    library.getHandler().unregisterPlayer(p);
    Logger.info("Unregistered Player " + p.getUniqueId());
  }
}
