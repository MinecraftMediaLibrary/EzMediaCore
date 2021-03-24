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

package com.github.pulsebeat02.minecraftmedialibrary.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * The interface used for NMS modules for custom implementations of each server version. It is
 * useful for sending map packets to certain players when displaying map pixels.
 */
public interface PacketHandler {

  /**
   * Display.
   *
   * @param viewers the viewers
   * @param map the map
   * @param mapWidth the map width
   * @param mapHeight the map height
   * @param rgb the rgb
   * @param videoWidth the video width
   * @param xOffset the x offset
   * @param yOffset the y offset
   */
  void display(
      UUID[] viewers,
      int map,
      int mapWidth,
      int mapHeight,
      ByteBuffer rgb,
      int videoWidth,
      int xOffset,
      int yOffset);

  /**
   * Display.
   *
   * @param viewers the viewers
   * @param map the map
   * @param mapWidth the map width
   * @param mapHeight the map height
   * @param rgb the rgb
   * @param videoWidth the video width
   */
  void display(
      UUID[] viewers, int map, int mapWidth, int mapHeight, ByteBuffer rgb, int videoWidth);

  /**
   * Display.
   *
   * @param viewers the viewers
   * @param entities the entities
   * @param data the data
   * @param width the width
   */
  void display(UUID[] viewers, Entity[] entities, int[] data, int width);

  /**
   * Registers player.
   *
   * @param player the player
   */
  void registerPlayer(Player player);

  /**
   * Unregisters player.
   *
   * @param player the player
   */
  void unregisterPlayer(Player player);

  /**
   * Checks if the map is registered.
   *
   * @param id the id
   * @return the boolean
   */
  boolean isMapRegistered(int id);

  /**
   * Unregisters map.
   *
   * @param id the id
   */
  void unregisterMap(int id);

  /**
   * Registers map.
   *
   * @param id the id
   */
  void registerMap(int id);

  /**
   * Called when packet is intercepted out.
   *
   * @param viewer the viewer
   * @param packet the packet
   * @return the object
   */
  Object onPacketInterceptOut(Player viewer, Object packet);

  /**
   * Called when packet is intercepted in.
   *
   * @param viewer the viewer
   * @param packet the packet
   * @return the object
   */
  Object onPacketInterceptIn(Player viewer, Object packet);
}
