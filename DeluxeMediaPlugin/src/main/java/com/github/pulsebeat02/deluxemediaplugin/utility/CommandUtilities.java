/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin.utility;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CommandUtilities {

  private static HashMap<String, Command> knownCommands = null;

  static {
    final String ver = Bukkit.getVersion();
    if (Pattern.compile("1\\.(?:13|14|15|16|17)").matcher(ver).find()) {
      final Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
      final SimpleCommandMap commandMap = (SimpleCommandMap) result;
      final Object map = getPrivateField(Objects.requireNonNull(commandMap), "knownCommands");
      knownCommands = (HashMap<String, Command>) map;
    } else if (Pattern.compile("1\\.(?:8|9|10|11|12)").matcher(ver).find()) {
      final Object result =
          getPrivateFieldLegacy(Bukkit.getServer().getPluginManager(), "commandMap");
      final SimpleCommandMap commandMap = (SimpleCommandMap) result;
      final Object map = getPrivateFieldLegacy(Objects.requireNonNull(commandMap), "knownCommands");
      knownCommands = (HashMap<String, Command>) map;
    } else {
      Bukkit.getLogger()
          .log(
              Level.SEVERE,
              "[DeluxeMediaPlugin] You are using a severely "
                  + "outdated version of Minecraft. Please update as versions below 1.8 are not "
                  + "supported. We are sorry of the inconvenience.");
      DeluxeMediaPlugin.OUTDATED = true;
    }
  }

  public static void unRegisterBukkitCommand(
      @NotNull final DeluxeMediaPlugin plugin, final PluginCommand cmd) {
    if (cmd == null) {
      return;
    }
    try {
      knownCommands.remove(cmd.getName());
      for (final String alias : cmd.getAliases()) {
        if (knownCommands.containsKey(alias)
            && knownCommands.get(alias).toString().contains(plugin.getName())) {
          knownCommands.remove(alias);
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static Object getPrivateField(@NotNull final Object object, @NotNull final String field) {
    try {
      final Class<?> clazz = object.getClass();
      final Field objectField =
          field.equals("commandMap")
              ? clazz.getDeclaredField(field)
              : field.equals("knownCommands")
                  ? clazz.getSuperclass().getDeclaredField(field)
                  : clazz.getDeclaredField(field);
      objectField.setAccessible(true);
      final Object result = objectField.get(object);
      objectField.setAccessible(false);
      return result;
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Object getPrivateFieldLegacy(
      @NotNull final Object object, @NotNull final String field) {
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
