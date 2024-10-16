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
package io.github.pulsebeat02.ezmediacore.nms.impl.v1_21_R1;

import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class NMSMapPacketInterceptor implements PacketHandler {

  private static final int PACKET_THRESHOLD_MS = 10;

  private final Map<UUID, ServerGamePacketListenerImpl> connections = new ConcurrentHashMap<>();
  private final Map<UUID, Long> lastUpdated = new ConcurrentHashMap<>();

  @Override
  public void displayDebugMarker(
          final UUID[] viewers,
          final String character,
          final int y,
          final int z,
          final int x,
          final int color,
          final int time) {
    final ByteBuf buf = Unpooled.buffer();
    final long packed = ((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12;
    final BlockPos pos = BlockPos.of(packed);
    final GameTestAddMarkerDebugPayload payload = new GameTestAddMarkerDebugPayload(pos, color, character, time);
    final ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
    for (final UUID uuid : viewers) {
      final ServerGamePacketListenerImpl connection = this.connections.get(uuid);
      if (connection == null) {
        continue;
      }
      connection.send(packet);
    }
  }

  private static void writeString(final ByteBuf packet, final String s) {
    final byte[] bytes = s.getBytes();
    int length = bytes.length;
    while ((length & -128) != 0) {
      packet.writeByte(length & 127 | 128);
      length >>>= 7;
    }
    packet.writeByte(length);
    packet.writeBytes(bytes);
  }

  @Override
  public void displayMaps(
          final UUID[] viewers,
          final byte[] rgb,
          final int map,
          final int height,
          final int width,
          final int videoWidth,
          final int xOff,
          final int yOff) {
    final int vidHeight = rgb.length / videoWidth;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + vidHeight;
    final int xLoopMin = Math.max(0, xOff >> 7);
    final int yLoopMin = Math.max(0, yOff >> 7);
    final int xLoopMax = Math.min(width, (int) Math.ceil(negXOff / 128.0));
    final int yLoopMax = Math.min(height, (int) Math.ceil(negYOff / 128.0));
    final ClientboundMapItemDataPacket[] packetArray =
            new ClientboundMapItemDataPacket[(xLoopMax - xLoopMin) * (yLoopMax - yLoopMin)];
    int arrIndex = 0;
    for (int y = yLoopMin; y < yLoopMax; y++) {

      final int relY = y << 7;
      final int topY = Math.max(0, yOff - relY);
      final int yDiff = Math.min(128 - topY, negYOff - (relY + topY));

      for (int x = xLoopMin; x < xLoopMax; x++) {

        final int relX = x << 7;
        final int topX = Math.max(0, xOff - relX);
        final int xDiff = Math.min(128 - topX, negXOff - (relX + topX));
        final int xPixMax = xDiff + topX;
        final int yPixMax = yDiff + topY;
        final byte[] mapData = new byte[xDiff * yDiff];

        //        IMPLEMENTATION #1
        //        IntStream.range(topY, yPixMax).parallel().forEach(iy -> {
        //          final int yPos = relY + iy;
        //          final int indexY = (yPos - yOff) * videoWidth;
        //          IntStream.range(topX, xPixMax).parallel().forEach(ix -> {
        //            final int val = (iy - topY) * xDiff + ix - topX;
        //            mapData[val] = rgb.get(indexY + relX + ix - xOff);
        //          });
        //        });

        //        IMPLEMENTATION #2
        //        IntStream.range(topY, yPixMax).parallel().forEach(iy -> {
        //          final int yPos = relY + iy;
        //          final int indexY = (yPos - yOff) * videoWidth;
        //          for (int ix = topX; ix < xPixMax; ix++) {
        //            final int val = (iy - topY) * xDiff + ix - topX;
        //            mapData[val] = rgb.get(indexY + relX + ix - xOff);
        //          }
        //        });

        for (int iy = topY; iy < yPixMax; iy++) {
          final int yPos = relY + iy;
          final int indexY = (yPos - yOff) * videoWidth;
          for (int ix = topX; ix < xPixMax; ix++) {
            mapData[(iy - topY) * xDiff + ix - topX] = rgb[indexY + relX + ix - xOff];
          }
        }

        final MapId mapId = new MapId(map + width * y + x);
        final byte b = (byte) 0;
        final boolean display = false;
        final List<MapDecoration> icons = new ArrayList<>();
        final MapItemSavedData.MapPatch worldmap = new MapItemSavedData.MapPatch(topX, topY, xDiff, yDiff, mapData);
        final ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(mapId, b, display, icons, worldmap);
        packetArray[arrIndex++] = packet;
      }
    }
    this.sendMapPackets(viewers, packetArray);
  }

  private void sendMapPackets(
          final UUID[] viewers, final ClientboundMapItemDataPacket[] packetArray) {
    if (viewers == null) {
      this.sendMapPacketsToAll(packetArray);
    } else {
      this.sendMapPacketsToSpecified(viewers, packetArray);
    }
  }

  private void sendMapPacketsToSpecified(
          final UUID[] viewers, final ClientboundMapItemDataPacket[] packetArray) {
    for (final UUID uuid : viewers) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  private void sendMapPacketsToAll(final ClientboundMapItemDataPacket[] packetArray) {
    for (final UUID uuid : this.connections.keySet()) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  @Override
  public void displayChat(
          final UUID[] viewers,
          final int[] data,
          final String character,
          final int width,
          final int height) {
    for (int y = 0; y < height; ++y) {
      this.displayToUsers(viewers, data, character, width, y);
    }
  }

  private void displayToUsers(
          final UUID[] viewers,
          final int[] data,
          final String character,
          final int width,
          final int y) {
    for (final UUID uuid : viewers) {
      this.displayComponent(data, character, width, y, uuid);
    }
  }

  private void displayComponent(
          final int[] data,
          final String character,
          final int width,
          final int y,
          final UUID uuid) {

    final ServerGamePacketListenerImpl connection = this.connections.get(uuid);
    if (connection == null) {
      return;
    }

    final Component[] base =
            CraftChatMessage.fromString(this.createChatComponent(character, data, width, y));
    for (final Component component : base) {
      connection.send(new ClientboundSystemChatPacket(component, true));
    }
  }

  @Override
  public void displayEntities(
          final UUID[] viewers,
          final Entity[] entities,
          final int[] data,
          final String character,
          final int width,
          final int height) {
    final int maxHeight = Math.min(height, entities.length);
    final ClientboundSetEntityDataPacket[] packets = new ClientboundSetEntityDataPacket[maxHeight];
    int index = 0;
    for (int i = 0; i < maxHeight; i++) {
      final MutableComponent component = MutableComponent.create(PlainTextContents.EMPTY);
      for (int x = 0; x < width; x++) {
        this.modifyComponent(character, component, data[index++]);
      }
      packets[i] = this.createEntityPacket(entities[i], component);
    }
    this.sendEntityPackets(viewers, packets);
  }

  private void sendEntityPackets(
          final UUID[] viewers, final ClientboundSetEntityDataPacket[] packets) {
    if (viewers == null) {
      this.sendEntityPacketsToAll(packets);
    } else {
      this.sendEntityPacketsToSpecified(viewers, packets);
    }
  }

  private void sendEntityPacketsToSpecified(
          final UUID[] viewers,
          final ClientboundSetEntityDataPacket[] packets) {
    for (final UUID uuid : viewers) {
      this.sendEntityPacketToViewers(uuid, packets);
    }
  }

  private void sendEntityPacketsToAll(final ClientboundSetEntityDataPacket[] packets) {
    for (final UUID uuid : this.connections.keySet()) {
      this.sendEntityPacketToViewers(uuid, packets);
    }
  }

  private void modifyComponent(
          final String character,
          final MutableComponent component,
          final int c) {
    final MutableComponent p = MutableComponent.create(PlainTextContents.EMPTY);
    p.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(c & 0xFFFFFF)));
    p.append(Component.literal(character));
    component.append(p);
  }


  private ClientboundSetEntityDataPacket createEntityPacket(
          final Entity entity, final Component component) {
    final CraftEntity craft = (CraftEntity) entity;
    final net.minecraft.world.entity.Entity nms = craft.getHandle();
    final SynchedEntityData watcher = nms.getEntityData();
    final int entityId = ((CraftEntity) entity).getHandle().getId();
    final List<SynchedEntityData.DataValue<?>> raw = watcher.getNonDefaultValues();
    final List<SynchedEntityData.DataValue<?>> values = new ArrayList<>(raw);
    final Iterator<SynchedEntityData.DataValue<?>> iterator = values.iterator();
    while (iterator.hasNext()) {
      final SynchedEntityData.DataValue<?> value = iterator.next();
      final int id = value.id();
      if (id == 2) {
        iterator.remove();
      }
    }
    values.add(new SynchedEntityData.DataValue<>(2, EntityDataSerializers.OPTIONAL_COMPONENT, Optional.of(component)));
    return new ClientboundSetEntityDataPacket(entityId, values);
  }

  private void sendEntityPacketToViewers(
          final UUID uuid, final ClientboundSetEntityDataPacket[] packets) {

    final ServerGamePacketListenerImpl connection = this.connections.get(uuid);
    if (connection == null) {
      return;
    }

    for (final ClientboundSetEntityDataPacket packet : packets) {
      connection.send(packet);
    }
  }

  private void sendMapPacketsToViewers(
          final UUID uuid, final ClientboundMapItemDataPacket[] packetArray) {
    final long val = this.lastUpdated.getOrDefault(uuid, 0L);
    if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
      final ServerGamePacketListenerImpl connection = this.connections.get(uuid);
      if (connection == null) {
        return;
      }
      this.updateTime(uuid);
      this.sendSeparatePackets(packetArray, connection);
    }
  }

  private void updateTime(final UUID uuid) {
    this.lastUpdated.put(uuid, System.currentTimeMillis());
  }

  private void sendSeparatePackets(
          final ClientboundMapItemDataPacket[] packetArray, final ServerGamePacketListenerImpl connection) {

    if (connection == null) {
      return;
    }

    for (final ClientboundMapItemDataPacket packet : packetArray) {
      connection.send(packet);
    }
  }

  @Override
  public void injectPlayer(final Player player) {
    final ServerGamePacketListenerImpl conn = ((CraftPlayer) player).getHandle().connection;
    final Channel channel = conn.connection.channel;
    this.addConnection(player, conn);
  }

  private void addConnection(final Player player, final ServerGamePacketListenerImpl conn) {
    this.connections.put(player.getUniqueId(), conn);
  }

  @Override
  public void uninjectPlayer(final Player player) {
    final Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
    this.removeConnection(player);
  }

  private void removeConnection(final Player player) {
    this.connections.remove(player.getUniqueId());
  }
}
