package com.github.pulsebeat02.deluxemediaplugin.utility;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandUtilities {

    private static HashMap<String, Command> knownCommands = null;

    static {
        try {
            final Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            final SimpleCommandMap commandMap = (SimpleCommandMap) result;
            final Object map = getPrivateField(commandMap, "knownCommands");
            knownCommands = (HashMap<String, Command>) map;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void unRegisterBukkitCommand(@NotNull final DeluxeMediaPlugin plugin, @NotNull final PluginCommand cmd) {
        try {
            knownCommands.remove(cmd.getName());
            for (final String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(plugin.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    private static Object getPrivateField(@NotNull final Object object, @NotNull final String field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Class<?> clazz = object.getClass();
        final Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        final Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

}
