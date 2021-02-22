/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface PacketHandler {

  void display(
      UUID[] viewers,
      int map,
      int mapWidth,
      int mapHeight,
      ByteBuffer rgb,
      int videoWidth,
      int xOffset,
      int yOffset);

  void display(
      UUID[] viewers, int map, int mapWidth, int mapHeight, ByteBuffer rgb, int videoWidth);

  void display(UUID[] viewers, Entity[] entities, int[] data, int width);

  void registerPlayer(Player player);

  void unregisterPlayer(Player player);

  boolean isMapRegistered(int id);

  void unregisterMap(int id);

  void registerMap(int id);

  Object onPacketInterceptOut(Player viewer, Object packet);

  Object onPacketInterceptIn(Player viewer, Object packet);
}
