package com.github.pulsebeat02.deluxemediaplugin.utility;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class CommandUtilities {

    private static final HashMap<String, Command> knownCommands;

    static {
        boolean usingLegacy = false;
        final String ver = Bukkit.getVersion();
        final Set<String> legacyVersions = ImmutableSet.of("1.8", "1.9", "1.10", "1.11", "1.12");
        for (final String str : legacyVersions) {
            if (ver.contains(str)) {
                usingLegacy = true;
                break;
            }
        }
        if (usingLegacy) {
            final Object result = getPrivateFieldLegacy(Bukkit.getServer().getPluginManager(), "commandMap");
            final SimpleCommandMap commandMap = (SimpleCommandMap) result;
            final Object map = getPrivateFieldLegacy(Objects.requireNonNull(commandMap), "knownCommands");
            knownCommands = (HashMap<String, Command>) map;
        } else {
            final Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            final SimpleCommandMap commandMap = (SimpleCommandMap) result;
            final Object map = getPrivateField(Objects.requireNonNull(commandMap), "knownCommands");
            knownCommands = (HashMap<String, Command>) map;
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

    private static Object getPrivateField(final Object object, final String field) {
        try {
            final Class<?> clazz = object.getClass();
            final Field objectField = field.equals("commandMap") ? clazz.getDeclaredField(field)
                    : field.equals("knownCommands") ? clazz.getSuperclass().getDeclaredField(field) : clazz.getDeclaredField(field);
            objectField.setAccessible(true);
            final Object result = objectField.get(object);
            objectField.setAccessible(false);
            return result;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getPrivateFieldLegacy(final Object object, final String field) {
        try {
            final Class<?> clazz = object.getClass();
            final Field objectField = clazz.getDeclaredField(field);
            objectField.setAccessible(true);
            final Object result = objectField.get(object);
            objectField.setAccessible(false);
            return result;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
