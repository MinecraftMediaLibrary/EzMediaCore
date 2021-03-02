/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/2/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.listener;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
  private void onPlayerJoin(final PlayerJoinEvent event) {
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
  private void onPlayerLeave(final PlayerQuitEvent event) {
    final Player p = event.getPlayer();
    library.getHandler().unregisterPlayer(p);
    Logger.info("Unregistered Player " + p.getUniqueId());
  }
}
