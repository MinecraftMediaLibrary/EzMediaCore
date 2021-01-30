package com.github.pulsebeat02.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface PacketHandler {

    void display(UUID[] viewers, long map, int mapWidth, int mapHeight, ByteBuffer rgb, int videoWidth, int xOffset, int yOffset);

    void display(UUID[] viewers, long map, int mapWidth, int mapHeight, ByteBuffer rgb, int videoWidth);

    void display(UUID[] viewers, Entity[] entities, int[] data, int width);

    void registerPlayer(Player player);

    void unregisterPlayer(Player player);

    boolean isMapRegistered(int id);

    void unregisterMap(long id);

    void registerMap(long id);

    Object onPacketInterceptOut(Player viewer, Object packet);

    Object onPacketInterceptIn(Player viewer, Object packet);

}
