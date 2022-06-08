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

package io.github.pulsebeat02.ezmediacore.nms;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import java.nio.IntBuffer;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public interface PacketHandler {

  void displayDebugMarker(
      @NotNull final UUID[] viewers,
      final int x,
      final int y,
      final int z,
      final int color,
      final int time);

  void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final BufferCarrier rgb,
      final int map,
      final int mapHeight,
      final int mapWidth,
      final int videoWidth,
      final int xOffset,
      final int yOffset);

  default void displayMaps(
      @NotNull final UUID[] viewers,
      @NotNull final BufferCarrier rgb,
      final int map,
      final int mapWidth,
      final int mapHeight,
      final int videoWidth) {
    final int vidHeight = rgb.getCapacity() / videoWidth;
    final int pixW = mapWidth << 7;
    final int pixH = mapHeight << 7;
    final int xOff = (pixW - videoWidth) >> 1;
    final int yOff = (pixH - vidHeight) >> 1;
    this.displayMaps(viewers, rgb, map, mapHeight, mapWidth, videoWidth, xOff, yOff);
  }

  void displayEntities(
      @NotNull final UUID[] viewers,
      @NotNull final Entity[] entities,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height);

  void displayChat(
      @NotNull final UUID[] viewers,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height);

  default void displayScoreboard(
      @NotNull final UUID[] viewers,
      @NotNull final Scoreboard scoreboard,
      @NotNull final IntBuffer data,
      final String character,
      final int width,
      final int height) {
    for (int y = 0; y < height; ++y) {
      StringBuilder msg;
      final Team team = scoreboard.getTeam("SLOT_" + y);
      if (team != null) {
        team.setSuffix(this.createChatComponent(character, data, width, y));
      }
    }
  }

  void injectPlayer(@NotNull final Player player);

  void uninjectPlayer(@NotNull final Player player);

  boolean isMapRegistered(final int id);

  void unregisterMap(final int id);

  void registerMap(final int id);

  default @NotNull String createChatComponent(
      final String character, @NotNull final IntBuffer data, final int width, final int y) {
    int before = -1;
    final StringBuilder msg = new StringBuilder();
    for (int x = 0; x < width; ++x) {
      final int rgb = this.appendRGB(msg, data, width, y, before, x);
      msg.append(character);
      before = rgb;
    }
    return msg.toString();
  }

  private int appendRGB(
      @NotNull final StringBuilder msg,
      @NotNull final IntBuffer data,
      final int width,
      final int y,
      final int before,
      final int x) {
    final int rgb = data.get(width * y + x);
    if (before != rgb) {
      msg.append(ChatColor.of("#" + Integer.toHexString(rgb).substring(2)));
    }
    return rgb;
  }
}
