package io.github.pulsebeat02.epicmedialib.nms.impl.v1_17_R1;

import io.github.pulsebeat02.epicmedialib.nms.PacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSMapPacketIntercepter implements PacketHandler {

  public static final int PACKET_THRESHOLD_MS = 0;
  private static final Set<Object> PACKET_DIFFERENTIATION;

  static {
    PACKET_DIFFERENTIATION = Collections.newSetFromMap(new WeakHashMap<>());
  }

  private final Map<UUID, PlayerConnection> playerConnections = new ConcurrentHashMap<>();
  private final Map<UUID, Long> lastUpdated = new ConcurrentHashMap<>();
  private final Set<Integer> maps = new TreeSet<>();
  private final MinecraftKey debugMarker = new MinecraftKey("debug/game_test_add_marker");

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
      this.playerConnections.get(uuid).sendPacket(packet);
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
      for (final UUID uuid : this.playerConnections.keySet()) {
        final long val = this.lastUpdated.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
          this.lastUpdated.put(uuid, System.currentTimeMillis());
          final PlayerConnection connection = this.playerConnections.get(uuid);
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
          final PlayerConnection connection = this.playerConnections.get(uuid);
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
    displayMaps(viewers, map, width, height, rgb, videoWidth, xOff, yOff);
  }

  @Override
  public void displayEntities(
      final UUID[] viewers, final Entity[] entities, final int[] data, final int width) {
    final int height = data.length / width;
    final int maxHeight = Math.min(height, entities.length);
    final PacketPlayOutEntityMetadata[] packets = new PacketPlayOutEntityMetadata[maxHeight];
    int index = 0;
    for (int i = 0; i < maxHeight; i++) {
      final net.minecraft.world.entity.Entity entity = ((CraftEntity) entities[i]).getHandle();
      final ChatComponentText component = new ChatComponentText("");
      for (int x = 0; x < width; x++) {
        final int c = data[index++];
        final ChatComponentText p = new ChatComponentText("█");
        p.setChatModifier(p.getChatModifier().setColor(ChatHexColor.a(c & 0xFFFFFF)));
        component.addSibling(p);
      }
      final PacketPlayOutEntityMetadata packet =
          new PacketPlayOutEntityMetadata(entity.getId(), new DataWatcher(entity), false);
      packets[i] = packet;
      PACKET_DIFFERENTIATION.add(packet);
    }
    if (viewers == null) {
      for (final UUID uuid : this.playerConnections.keySet()) {
        final PlayerConnection connection = this.playerConnections.get(uuid);
        for (final PacketPlayOutEntityMetadata packet : packets) {
          connection.sendPacket(packet);
        }
      }
    } else {
      for (final UUID uuid : viewers) {
        final PlayerConnection connection = this.playerConnections.get(uuid);
        if (connection != null) {
          for (final PacketPlayOutEntityMetadata packet : packets) {
            connection.sendPacket(packet);
          }
        }
      }
    }
  }

  @Override
  public void registerPlayer(final Player player) {
    this.playerConnections.put(player.getUniqueId(), ((CraftPlayer) player).getHandle().b);
  }

  @Override
  public void unregisterPlayer(final Player player) {
    this.playerConnections.remove(player.getUniqueId());
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
    if (packet instanceof PacketPlayOutMap && PACKET_DIFFERENTIATION.contains(packet)) {
      return packet;
    }
    return packet;
  }

  @Override
  public Object onPacketInterceptIn(final Player viewer, final Object packet) {
    return packet;
  }
}
