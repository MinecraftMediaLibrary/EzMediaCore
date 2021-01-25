package com.github.pulsebeat02.reflection;

import com.github.pulsebeat02.nms.PacketHandler;
import org.bukkit.Bukkit;

public class NMSReflectionManager {

    public static final String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static PacketHandler getNewPacketHandlerInstance() {
        try {
            Class<?> clazz = Class.forName("com.github.pulsebeat02.nms.impl" + VERSION + ".NMSMapPacketIntercepter");
            return (PacketHandler) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
