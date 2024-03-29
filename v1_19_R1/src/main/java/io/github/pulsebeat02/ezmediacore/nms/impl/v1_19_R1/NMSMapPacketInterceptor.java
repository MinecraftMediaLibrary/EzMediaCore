package io.github.pulsebeat02.ezmediacore.nms.impl.v1_19_R1;

import static io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils.setFinalField;

import io.github.pulsebeat02.ezmediacore.callback.buffer.BufferCarrier;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.utility.unsafe.UnsafeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcher.Item;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("CommentedOutCode")
public final class NMSMapPacketInterceptor implements PacketHandler {

  private static final int PACKET_THRESHOLD_MS;
  private static final Set<Object> PACKET_DIFFERENTIATION;
  private static final Field METADATA_ITEMS;
  private static final Field CHATMODIFIER;

  static {
    PACKET_THRESHOLD_MS = 0;
    PACKET_DIFFERENTIATION = Collections.newSetFromMap(new WeakHashMap<>());
    try {
      METADATA_ITEMS = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
      CHATMODIFIER = IChatMutableComponent.class.getDeclaredField("e");
      METADATA_ITEMS.setAccessible(true);
    } catch (final NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  private final Map<UUID, PlayerConnection> connections;
  private final Map<UUID, Long> lastUpdated;
  private final Set<Integer> maps;
  private final MinecraftKey debugMarker;
  private final String handlerName;

  {
    this.connections = new ConcurrentHashMap<>();
    this.lastUpdated = new ConcurrentHashMap<>();
    this.maps = new TreeSet<>();
    this.debugMarker = new MinecraftKey("debug/game_test_add_marker");
    this.handlerName = "ezmediacore_handler_1171";
  }

  @Override
  public void displayDebugMarker(
      final UUID @NotNull [] viewers,
      @NotNull final String character, final int y, final int z, final int x,
      final int color,
      final int time) {
    final ByteBuf buf = Unpooled.buffer();
    buf.writeLong(((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12);
    buf.writeInt(color);
    writeString(buf, character);
    buf.writeInt(time);
    final PacketPlayOutCustomPayload packet =
        new PacketPlayOutCustomPayload(this.debugMarker, new PacketDataSerializer(buf));
    for (final UUID uuid : viewers) {
      final PlayerConnection connection = this.connections.get(uuid);
      if (connection == null) {
        continue;
      }
      connection.a(packet);
    }
  }

  private static void writeString(@NotNull final ByteBuf packet, @NotNull final String s) {
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
      @NotNull final BufferCarrier rgb,
      final int map,
      final int height,
      final int width,
      final int videoWidth,
      final int xOff,
      final int yOff) {
    final int vidHeight = rgb.getCapacity() / videoWidth;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + vidHeight;
    final int xLoopMin = Math.max(0, xOff >> 7);
    final int yLoopMin = Math.max(0, yOff >> 7);
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
            mapData[(iy - topY) * xDiff + ix - topX] = rgb.getByte(indexY + relX + ix - xOff);
          }
        }

        final int mapId = map + width * y + x;
        final byte b = (byte) 0;
        final boolean display = false;
        final List<MapIcon> icons = new ArrayList<>();
        final WorldMap.b worldmap = new WorldMap.b(topX, topY, xDiff, yDiff, mapData);

        final PacketPlayOutMap packet = new PacketPlayOutMap(mapId, b, display, icons, worldmap);

        packetArray[arrIndex++] = packet;
        PACKET_DIFFERENTIATION.add(packet);
      }
    }
    this.sendMapPackets(viewers, packetArray);
  }

  private void sendMapPackets(
      @NotNull final UUID[] viewers, @NotNull final PacketPlayOutMap[] packetArray) {
    if (viewers == null) {
      this.sendMapPacketsToAll(packetArray);
    } else {
      this.sendMapPacketsToSpecified(viewers, packetArray);
    }
  }

  private void sendMapPacketsToSpecified(
      @NotNull final UUID @NotNull [] viewers, @NotNull final PacketPlayOutMap[] packetArray) {
    for (final UUID uuid : viewers) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  private void sendMapPacketsToAll(@NotNull final PacketPlayOutMap[] packetArray) {
    for (final UUID uuid : this.connections.keySet()) {
      this.sendMapPacketsToViewers(uuid, packetArray);
    }
  }

  @Override
  public void displayChat(
      final UUID[] viewers,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height) {
    for (int y = 0; y < height; ++y) {
      this.displayToUsers(viewers, data, character, width, y);
    }
  }

  private void displayToUsers(
      @NotNull final UUID[] viewers,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int y) {
    for (final UUID uuid : viewers) {
      this.displayComponent(data, character, width, y, uuid);
    }
  }

  private void displayComponent(
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int y,
      final UUID uuid) {

    final PlayerConnection connection = this.connections.get(uuid);
    if (connection == null) {
      return;
    }

    final IChatBaseComponent[] base =
        CraftChatMessage.fromString(this.createChatComponent(character, data, width, y));
    for (final IChatBaseComponent component : base) {
      connection.a(new ClientboundSystemChatPacket(component, 1));
    }
  }

  @Override
  public void displayEntities(
      @NotNull final UUID[] viewers,
      @NotNull final Entity[] entities,
      @NotNull final IntBuffer data,
      @NotNull final String character,
      final int width,
      final int height) {
    final int maxHeight = Math.min(height, entities.length);
    final PacketPlayOutEntityMetadata[] packets = new PacketPlayOutEntityMetadata[maxHeight];
    int index = 0;
    for (int i = 0; i < maxHeight; i++) {
      final IChatMutableComponent component = IChatMutableComponent.a(ComponentContents.a);
      for (int x = 0; x < width; x++) {
        this.modifyComponent(character, component, data.get(index++));
      }
      packets[i] = this.createEntityPacket(entities[i], component);
    }
    this.sendEntityPackets(viewers, packets);
  }

  private void sendEntityPackets(
      @NotNull final UUID[] viewers, @NotNull final PacketPlayOutEntityMetadata[] packets) {
    if (viewers == null) {
      this.sendEntityPacketsToAll(packets);
    } else {
      this.sendEntityPacketsToSpecified(viewers, packets);
    }
  }

  private void sendEntityPacketsToSpecified(
      @NotNull final UUID @NotNull [] viewers,
      @NotNull final PacketPlayOutEntityMetadata[] packets) {
    for (final UUID uuid : viewers) {
      this.sendEntityPacketToViewers(uuid, packets);
    }
  }

  private void sendEntityPacketsToAll(@NotNull final PacketPlayOutEntityMetadata[] packets) {
    for (final UUID uuid : this.connections.keySet()) {
      this.sendEntityPacketToViewers(uuid, packets);
    }
  }

  private void modifyComponent(
      @NotNull final String character,
      @NotNull final IChatMutableComponent component,
      final int c) {
    final IChatMutableComponent p = IChatMutableComponent.a(ComponentContents.a);
    UnsafeUtils.setFinalField(CHATMODIFIER, p, ChatModifier.a.a(ChatHexColor.a(c & 0xFFFFFF)));
    p.a(IChatBaseComponent.a(character));
    component.a(p);
  }

  @NotNull
  private PacketPlayOutEntityMetadata createEntityPacket(
      @NotNull final Entity entity, @NotNull final IChatBaseComponent component) {

    final int id = ((CraftEntity) entity).getHandle().ae();
    final DataWatcher watcher = new DataWatcher(null);
    final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(id, watcher, false);

    final Optional<IChatBaseComponent> optional = Optional.of(component);
    final DataWatcherObject<Optional<IChatBaseComponent>> object =
        new DataWatcherObject<>(2, DataWatcherRegistry.f);
    final Item<Optional<IChatBaseComponent>> item = new Item<>(object, optional);
    final List<Item<Optional<IChatBaseComponent>>> list = Collections.singletonList(item);

    setFinalField(METADATA_ITEMS, packet, list);

    return packet;
  }

  private void sendEntityPacketToViewers(
      @NotNull final UUID uuid, @NotNull final PacketPlayOutEntityMetadata @NotNull [] packets) {

    final PlayerConnection connection = this.connections.get(uuid);
    if (connection == null) {
      return;
    }

    for (final PacketPlayOutEntityMetadata packet : packets) {
      connection.a(packet);
    }
  }

  private void sendMapPacketsToViewers(
      @NotNull final UUID uuid, @NotNull final PacketPlayOutMap[] packetArray) {
    final long val = this.lastUpdated.getOrDefault(uuid, 0L);
    if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
      final PlayerConnection connection = this.connections.get(uuid);
      if (connection == null) {
        return;
      }
      this.updateTime(uuid);
      this.sendSeparatePackets(packetArray, connection);
    }
  }

  private void updateTime(@NotNull final UUID uuid) {
    this.lastUpdated.put(uuid, System.currentTimeMillis());
  }

  private void sendSeparatePackets(
      @NotNull final PacketPlayOutMap[] packetArray, @Nullable final PlayerConnection connection) {

    if (connection == null) {
      return;
    }

    for (final PacketPlayOutMap packet : packetArray) {
      connection.a(packet);
    }
  }

  @Override
  public void injectPlayer(@NotNull final Player player) {
    final PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
    final Channel channel = conn.b.m;
    this.addChannelPipeline(channel);
    this.addConnection(player, conn);
  }

  private void addChannelPipeline(final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void addConnection(@NotNull final Player player, final PlayerConnection conn) {
    this.connections.put(player.getUniqueId(), conn);
  }

  @Override
  public void uninjectPlayer(@NotNull final Player player) {
    final Channel channel = ((CraftPlayer) player).getHandle().b.b.m;
    this.removeChannelPipeline(channel);
    this.removeConnection(player);
  }

  private void removeChannelPipeline(@Nullable final Channel channel) {
    if (channel != null) {
      this.removeChannelPipelineHandler(channel);
    }
  }

  private void removeChannelPipelineHandler(@NotNull final Channel channel) {
    final ChannelPipeline pipeline = channel.pipeline();
    if (pipeline.get(this.handlerName) != null) {
      pipeline.remove(this.handlerName);
    }
  }

  private void removeConnection(@NotNull final Player player) {
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
}
