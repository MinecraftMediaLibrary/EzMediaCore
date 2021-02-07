package com.github.pulsebeat02.minecraftmedialibrary.reflection;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class NMSReflectionManager {

    public static final String VERSION;

    static {
        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static PacketHandler getNewPacketHandlerInstance(@NotNull final MinecraftMediaLibrary library) {
        try {
            Logger.info("Loading NMS Class for Version " + VERSION);
            final Class<?> clazz = Class.forName("com.github.pulsebeat02.minecraftmedialibrary.nms.impl." + VERSION + ".NMSMapPacketIntercepter");
            return (PacketHandler) clazz.getDeclaredConstructor().newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("The Server Version you are using (" + VERSION + ") is not yet supported by MinecraftMediaLibrary! " +
                    "Shutting down due to the Fatal Error");
            library.shutdown();
            return null;
        }
    }

}
