package com.github.pulsebeat02.nms.impl.v1_16_R2;

import com.github.pulsebeat02.nms.PacketHandler;
import net.minecraft.server.v1_16_R2.ChatComponentText;
import net.minecraft.server.v1_16_R2.ChatHexColor;
import net.minecraft.server.v1_16_R2.DataWatcher;
import net.minecraft.server.v1_16_R2.DataWatcherObject;
import net.minecraft.server.v1_16_R2.DataWatcherRegistry;
import net.minecraft.server.v1_16_R2.IChatBaseComponent;
import net.minecraft.server.v1_16_R2.MapIcon;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R2.PacketPlayOutMap;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NMSMapPacketIntercepter implements PacketHandler {

    public static final int PACKET_THRESHOLD_MS = 0;

    private static final Field[] MAP_FIELDS = new Field[10];
    private static Field METADATA_ID;
    private static Field METADATA_ITEMS;

    static {
        try {
            MAP_FIELDS[0] = PacketPlayOutMap.class.getDeclaredField("a");
            MAP_FIELDS[1] = PacketPlayOutMap.class.getDeclaredField("b");
            MAP_FIELDS[2] = PacketPlayOutMap.class.getDeclaredField("c");
            MAP_FIELDS[3] = PacketPlayOutMap.class.getDeclaredField("d");
            MAP_FIELDS[4] = PacketPlayOutMap.class.getDeclaredField("e");
            MAP_FIELDS[5] = PacketPlayOutMap.class.getDeclaredField("f");
            MAP_FIELDS[6] = PacketPlayOutMap.class.getDeclaredField("g");
            MAP_FIELDS[7] = PacketPlayOutMap.class.getDeclaredField("h");
            MAP_FIELDS[8] = PacketPlayOutMap.class.getDeclaredField("i");
            MAP_FIELDS[9] = PacketPlayOutMap.class.getDeclaredField("j");
            for (Field field : MAP_FIELDS) {
                field.setAccessible(true);
            }
            METADATA_ID = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
            METADATA_ID.setAccessible(true);
            METADATA_ITEMS = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
            METADATA_ITEMS.setAccessible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private final Map<UUID, PlayerConnection> playerConnections = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastUpdated = new ConcurrentHashMap<>();
    private final Set<Long> maps = new TreeSet<>();

    @Override
    public void display(UUID[] viewers, long map, int width, int height, ByteBuffer rgb, int videoWidth) {
        int vidHeight = rgb.capacity() / videoWidth;
        int pixW = width << 7;
        int pixH = height << 7;
        int xOff = (pixW - videoWidth) / 2;
        int yOff = (pixH - vidHeight) / 2;
        display(viewers, map, width, height, rgb, videoWidth, xOff, yOff);
    }

    @Override
    public void display(UUID[] viewers, long map, int width, int height, ByteBuffer rgb, int videoWidth, int xOff,
                        int yOff) {
        int vidHeight = rgb.capacity() / videoWidth;
        int negXOff = xOff + videoWidth;
        int negYOff = yOff + vidHeight;
        int xLoopMin = Math.max(0, xOff / 128);
        int yLoopMin = Math.max(0, yOff / 128);
        int xLoopMax = Math.min(width, (int) Math.ceil(negXOff / 128.0));
        int yLoopMax = Math.min(height, (int) Math.ceil(negYOff / 128.0));
        PacketPlayOutMap[] packetArray = new PacketPlayOutMap[(xLoopMax - xLoopMin) * (yLoopMax - yLoopMin)];
        int arrIndex = 0;
        for (int y = yLoopMin; y < yLoopMax; y++) {
            int relY = y << 7;
            int topY = Math.max(0, yOff - relY);
            int yDiff = Math.min(128 - topY, negYOff - (relY + topY));
            for (int x = xLoopMin; x < xLoopMax; x++) {
                int relX = x << 7;
                int topX = Math.max(0, xOff - relX);
                int xDiff = Math.min(128 - topX, negXOff - (relX + topX));
                int xPixMax = xDiff + topX;
                int yPixMax = yDiff + topY;
                byte[] mapData = new byte[xDiff * yDiff];
                for (int iy = topY; iy < yPixMax; iy++) {
                    int yPos = relY + iy;
                    int indexY = (yPos - yOff) * videoWidth;
                    for (int ix = topX; ix < xPixMax; ix++) {
                        int val = (iy - topY) * xDiff + ix - topX;
                        mapData[val] = rgb.get(indexY + relX + ix - xOff);
                    }
                }
                long mapId = map + width * y + x;
                PacketPlayOutMap packet = new PacketPlayOutMap();
                try {
                    MAP_FIELDS[0].set(packet, mapId);
                    MAP_FIELDS[1].set(packet, (byte) 0);
                    MAP_FIELDS[2].set(packet, false);
                    MAP_FIELDS[3].set(packet, false);
                    MAP_FIELDS[4].set(packet, new MapIcon[0]);
                    MAP_FIELDS[5].set(packet, topX);
                    MAP_FIELDS[6].set(packet, topY);
                    MAP_FIELDS[7].set(packet, xDiff);
                    MAP_FIELDS[8].set(packet, yDiff);
                    MAP_FIELDS[9].set(packet, mapData);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                packetArray[arrIndex++] = packet;
            }
        }
        if (viewers == null) {
            for (UUID uuid : playerConnections.keySet()) {
                long val = lastUpdated.getOrDefault(uuid, 0L);
                if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
                    lastUpdated.put(uuid, System.currentTimeMillis());
                    PlayerConnection connection = playerConnections.get(uuid);
                    for (PacketPlayOutMap packet : packetArray) {
                        connection.sendPacket(packet);
                    }
                }
            }
        } else {
            for (UUID uuid : viewers) {
                long val = lastUpdated.getOrDefault(uuid, 0L);
                if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
                    lastUpdated.put(uuid, System.currentTimeMillis());
                    PlayerConnection connection = playerConnections.get(uuid);
                    if (connection != null) {
                        for (PacketPlayOutMap packet : packetArray) {
                            connection.sendPacket(packet);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void display(UUID[] viewers, Entity[] entities, int[] data, int width) {
        int height = data.length / width;
        int maxHeight = Math.min(height, entities.length);
        PacketPlayOutEntityMetadata[] packets = new PacketPlayOutEntityMetadata[maxHeight];
        int index = 0;
        for (int i = 0; i < maxHeight; i++) {
            int id = ((CraftEntity) entities[i]).getHandle().getId();
            ChatComponentText component = new ChatComponentText("");
            for (int x = 0; x < width; x++) {
                int c = data[index++];
                ChatComponentText p = new ChatComponentText("█");
                p.setChatModifier(p.getChatModifier().setColor(ChatHexColor.a(c & 0xFFFFFF)));
                component.addSibling(p);
            }
            DataWatcher.Item<Optional<IChatBaseComponent>> item = new DataWatcher.Item<>(
                    new DataWatcherObject<>(2, DataWatcherRegistry.f),
                    Optional.of(component));
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
            try {
                METADATA_ID.set(packet, id);
                METADATA_ITEMS.set(packet, Collections.singletonList(item));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            packets[i] = packet;
        }
        if (viewers == null) {
            for (UUID uuid : playerConnections.keySet()) {
                PlayerConnection connection = playerConnections.get(uuid);
                for (PacketPlayOutEntityMetadata packet : packets) {
                    connection.sendPacket(packet);
                }
            }
        } else {
            for (UUID uuid : viewers) {
                PlayerConnection connection = playerConnections.get(uuid);
                if (connection != null) {
                    for (PacketPlayOutEntityMetadata packet : packets) {
                        connection.sendPacket(packet);
                    }
                }
            }
        }
    }

    @Override
    public Object onPacketInterceptOut(Player viewer, Object packet) {
        if (packet instanceof PacketPlayOutMinimap) {
            return ((PacketPlayOutMinimap) packet).packet;
        }
        return packet;
    }

    @Override
    public Object onPacketInterceptIn(Player viewer, Object packet) {
        return packet;
    }

    @Override
    public void registerPlayer(Player player) {
        playerConnections.put(player.getUniqueId(), ((CraftPlayer) player).getHandle().playerConnection);
    }

    @Override
    public void unregisterPlayer(Player player) {
        playerConnections.remove(player.getUniqueId());
    }

    @Override
    public boolean isMapRegistered(int id) {
        return maps.contains(id);
    }

    @Override
    public void registerMap(long id) {
        maps.add(id);
    }

    @Override
    public void unregisterMap(long id) {
        maps.remove(id);
    }

    private static class PacketPlayOutMinimap extends PacketPlayOutMap {
        protected final PacketPlayOutMap packet;

        protected PacketPlayOutMinimap(PacketPlayOutMap packet) {
            this.packet = packet;
        }
    }

}
