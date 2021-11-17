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
package io.github.pulsebeat02.ezmediacore.nms.impl.v1_17_R1;

import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.SystemUtils;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcher.Item;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public final class NMSMapPacketInterceptor implements PacketHandler {

  private static final int PACKET_THRESHOLD_MS;
  private static final Set<Object> PACKET_DIFFERENTIATION;
  private static Field METADATA_ITEMS;

  static {
    PACKET_THRESHOLD_MS = 0;
    PACKET_DIFFERENTIATION = Collections.newSetFromMap(new WeakHashMap<>());
    try {
      METADATA_ITEMS = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
      METADATA_ITEMS.setAccessible(true);
    } catch (final NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  private final Map<UUID, Channel> channels;
  private final Map<UUID, PlayerConnection> connections;
  private final Map<UUID, Long> lastUpdated;
  private final Set<Integer> maps;
  private final MinecraftKey debugMarker;
  private final String handlerName;

  public NMSMapPacketInterceptor() {
    this.channels = new ConcurrentHashMap<>();
    this.connections = new ConcurrentHashMap<>();
    this.lastUpdated = new ConcurrentHashMap<>();
    this.maps = new TreeSet<>();
    this.debugMarker = new MinecraftKey("debug/game_test_add_marker");
    this.handlerName = "ezmediacore_handler_1171";
  }

  @Override
  public void displayDebugMarker(
      final UUID[] viewers,
      final int x,
      final int y,
      final int z,
      final int color,
      final int time) {
    final ByteBuf buf = Unpooled.buffer();
    buf.writeLong(((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12);
    buf.writeInt(color);
    buf.writeInt(time);
    final PacketPlayOutCustomPayload packet =
        new PacketPlayOutCustomPayload(this.debugMarker, new PacketDataSerializer(buf));
    for (final UUID uuid : viewers) {
      this.connections.get(uuid).sendPacket(packet);
    }
  }

  @Override
  public void displayMaps(
      final UUID[] viewers,
      final int map,
      final int width,
      final int height,
      final ByteBuffer rgb,
      final int videoWidth,
      final int xOff,
      final int yOff) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + vidHeight;
    final int xLoopMin = Math.max(0, xOff / 128);
    final int yLoopMin = Math.max(0, yOff / 128);
    final int xLoopMax = Math.min(width, (int) Math.ceil(negXOff / 128.0));
    final int yLoopMax = Math.min(height, (int) Math.ceil(negYOff / 128.0));
    final PacketPlayOutMap[] packetArray =
        new PacketPlayOutMap[(xLoopMax - xLoopMin) * (yLoopMax - yLoopMin)];
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
        for (int iy = topY; iy < yPixMax; iy++) {
          final int yPos = relY + iy;
          final int indexY = (yPos - yOff) * videoWidth;
          for (int ix = topX; ix < xPixMax; ix++) {
            final int val = (iy - topY) * xDiff + ix - topX;
            mapData[val] = rgb.get(indexY + relX + ix - xOff);
          }
        }
        final int mapId = map + width * y + x;
        final PacketPlayOutMap packet =
            new PacketPlayOutMap(
                mapId,
                (byte) 0,
                false,
                new ArrayList<>(),
                new WorldMap.b(topX, topY, xDiff, yDiff, mapData));
        packetArray[arrIndex++] = packet;
        PACKET_DIFFERENTIATION.add(packet);
      }
    }
    if (viewers == null) {
      for (final UUID uuid : this.connections.keySet()) {
        final long val = this.lastUpdated.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
          this.lastUpdated.put(uuid, System.currentTimeMillis());
          final PlayerConnection connection = this.connections.get(uuid);
          for (final PacketPlayOutMap packet : packetArray) {
            connection.sendPacket(packet);
          }
        }
      }
    } else {
      for (final UUID uuid : viewers) {
        final long val = this.lastUpdated.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
          this.lastUpdated.put(uuid, System.currentTimeMillis());
          final PlayerConnection connection = this.connections.get(uuid);
          if (connection != null) {
            for (final PacketPlayOutMap packet : packetArray) {
              connection.sendPacket(packet);
            }
          }
        }
      }
    }
  }

  @Override
  public void displayMaps(
      final UUID[] viewers,
      final int map,
      final int width,
      final int height,
      final ByteBuffer rgb,
      final int videoWidth) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int pixW = width << 7;
    final int pixH = height << 7;
    final int xOff = (pixW - videoWidth) >> 1;
    final int yOff = (pixH - vidHeight) >> 1;
    this.displayMaps(viewers, map, width, height, rgb, videoWidth, xOff, yOff);
  }

  @Override
  public void displayChat(
      final UUID[] viewers,
      final String character,
      final int[] data,
      final int width,
      final int height) {
    for (int y = 0; y < height; ++y) {
      int before = -1;
      final StringBuilder msg = new StringBuilder();
      for (int x = 0; x < width; ++x) {
        final int rgb = data[width * y + x];
        if (before != rgb) {
          msg.append(ChatColor.of("#" + Integer.toHexString(rgb).substring(2)));
        }
        msg.append(character);
        before = rgb;
      }
      for (final UUID uuid : viewers) {
        final PlayerConnection connection = this.connections.get(uuid);
        final IChatBaseComponent[] base = CraftChatMessage.fromString(msg.toString());
        for (final IChatBaseComponent component : base) {
          connection.sendPacket(new PacketPlayOutChat(component, ChatMessageType.b, SystemUtils.b));
        }
      }
    }
  }

  @Override
  public void displayScoreboard(
      final UUID[] viewers,
      final Scoreboard scoreboard,
      final String character,
      final int[] data,
      final int width,
      final int height) {
    for (int y = 0; y < height; ++y) {
      int before = -1;
      final StringBuilder msg = new StringBuilder();
      for (int x = 0; x < width; ++x) {
        final int rgb = data[width * y + x];
        if (before != rgb) {
          msg.append(ChatColor.of("#" + Integer.toHexString(rgb).substring(2)));
        }
        msg.append("█");
        before = rgb;
      }
      final Team team = scoreboard.getTeam("SLOT_" + y);
      if (team != null) {
        team.setSuffix(msg.toString());
      }
    }
  }

  @Override
  public void displayEntities(
      final UUID[] viewers,
      final Entity[] entities,
      final int[] data,
      final int width,
      final int height) {
    final int maxHeight = Math.min(height, entities.length);
    final PacketPlayOutEntityMetadata[] packets = new PacketPlayOutEntityMetadata[maxHeight];
    int index = 0;
    for (int i = 0; i < maxHeight; i++) {
      final ChatComponentText component = new ChatComponentText("");
      for (int x = 0; x < width; x++) {
        final int c = data[index++];
        final ChatComponentText p = new ChatComponentText("█");
        p.setChatModifier(p.getChatModifier().setColor(ChatHexColor.a(c & 0xFFFFFF)));
        component.addSibling(p);
      }
      final PacketPlayOutEntityMetadata packet =
          new PacketPlayOutEntityMetadata(
              ((CraftEntity) entities[i]).getHandle().getId(), new DataWatcher(null), false);
      try {
        UnsafeUtils.setFinalField(
            METADATA_ITEMS,
            packet,
            Collections.singletonList(
                new Item<>(
                    new DataWatcherObject<>(2, DataWatcherRegistry.f), Optional.of(component))));
      } catch (final IllegalArgumentException e) {
        e.printStackTrace();
      }
      packets[i] = packet;
    }
    if (viewers == null) {
      for (final UUID uuid : this.connections.keySet()) {
        final PlayerConnection connection = this.connections.get(uuid);
        for (final PacketPlayOutEntityMetadata packet : packets) {
          connection.sendPacket(packet);
        }
      }
    } else {
      for (final UUID uuid : viewers) {
        final PlayerConnection connection = this.connections.get(uuid);
        if (connection != null) {
          for (final PacketPlayOutEntityMetadata packet : packets) {
            connection.sendPacket(packet);
          }
        }
      }
    }
  }

  @Override
  public void injectPlayer(@NotNull final Player player) {
    final PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
    final Channel channel = conn.a.k;
    if (channel != null) {
      this.channels.put(player.getUniqueId(), channel);
      final ChannelPipeline pipeline = channel.pipeline();
      if (pipeline.get(this.handlerName) != null) {
        pipeline.remove(this.handlerName);
      }
      pipeline.addBefore("packet_handler", this.handlerName, new PacketInterceptor(player));
    }
    this.connections.put(player.getUniqueId(), conn);
  }

  @Override
  public void uninjectPlayer(@NotNull final Player player) {
    final Channel channel = ((CraftPlayer) player).getHandle().b.a.k;
    this.channels.remove(player.getUniqueId());
    if (channel != null) {
      final ChannelPipeline pipeline = channel.pipeline();
      if (pipeline.get(this.handlerName) != null) {
        pipeline.remove(this.handlerName);
      }
    }
    this.connections.remove(player.getUniqueId());
  }

  @Override
  public boolean isMapRegistered(final int id) {
    return this.maps.contains(id);
  }

  @Override
  public void unregisterMap(final int id) {
    this.maps.remove(id);
  }

  @Override
  public void registerMap(final int id) {
    this.maps.add(id);
  }

  @Override
  public Object onPacketInterceptOut(final Player viewer, final Object packet) {
    //    if (PACKET_DIFFERENTIATION.contains(packet)) {
    //      // some logic
    //      return packet;
    //    }
    return packet;
  }

  @Override
  public Object onPacketInterceptIn(final Player viewer, final Object packet) {
    return packet;
  }

  private class PacketInterceptor extends ChannelDuplexHandler {

    public volatile Player player;

    private PacketInterceptor(final Player player) {
      this.player = player;
    }

    @Override
    public void channelRead(@NotNull final ChannelHandlerContext ctx, Object msg) throws Exception {
      msg = NMSMapPacketInterceptor.this.onPacketInterceptIn(this.player, msg);
      if (msg != null) {
        super.channelRead(ctx, msg);
      }
    }

    @Override
    public void write(
        @NotNull final ChannelHandlerContext ctx, Object msg, @NotNull final ChannelPromise promise)
        throws Exception {
      msg = NMSMapPacketInterceptor.this.onPacketInterceptOut(this.player, msg);
      if (msg != null) {
        super.write(ctx, msg, promise);
      }
    }
  }
}
