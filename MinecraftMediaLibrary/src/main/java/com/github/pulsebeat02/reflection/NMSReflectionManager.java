package com.github.pulsebeat02.reflection;

import com.github.pulsebeat02.nms.PacketHandler;
import com.github.pulsebeat02.logger.Logger;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class NMSReflectionManager {

    public static final String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static PacketHandler getNewPacketHandlerInstance() {
        try {
            Logger.info("Loading NMS Class for Version " + VERSION);
            Class<?> clazz = Class.forName("com.github.pulsebeat02.nms.impl" + VERSION + ".NMSMapPacketIntercepter");
            return (PacketHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("The Server Version you are using (" + VERSION + ") is not yet supported by MinecraftMediaLibrary!");
            e.printStackTrace();
            return null;
        }
    }

}
