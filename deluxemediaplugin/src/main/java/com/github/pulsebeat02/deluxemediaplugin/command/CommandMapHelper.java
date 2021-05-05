/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/27/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;

public final class CommandMapHelper {

  private static final Method GET_COMMAND_MAP_METHOD;

  static {
    try {
      final Class<? extends Server> craftServerClass = Bukkit.getServer().getClass();
      GET_COMMAND_MAP_METHOD = craftServerClass.getDeclaredMethod("getCommandMap");
      GET_COMMAND_MAP_METHOD.setAccessible(true);
    } catch (final ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Uses Reflection to expose and retrieve the CommandMap.
   *
   * @return CommandMap map
   */
  public static CommandMap getCommandMap() {
    try {
      return (CommandMap) GET_COMMAND_MAP_METHOD.invoke(Bukkit.getServer());
    } catch (final ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }
}
