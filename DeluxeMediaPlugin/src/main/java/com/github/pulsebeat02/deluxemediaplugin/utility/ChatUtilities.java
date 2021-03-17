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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalLong;

public final class ChatUtilities {

  public static String formatMessage(@NotNull final String message) {
    return ChatColor.AQUA
        + "["
        + ChatColor.GOLD
        + "DeluxeMediaPlugin"
        + ChatColor.AQUA
        + "] "
        + message;
  }

  public static OptionalLong checkMapBoundaries(@NotNull final CommandSender sender, final String str) {
    final String message;
    final long id = checkLongValidity(str);
    if (id == Long.MIN_VALUE) {
      message = "is not a valid argument!";
    } else if (id < -2_147_483_647L) {
      message = "is too low!";
    } else if (id > 2_147_483_647L) {
      message = "is too high!";
    } else {
      return OptionalLong.of(id);
    }
    sender.sendMessage(
        ChatUtilities.formatMessage(
            String.join(
                " ",
                ChatColor.RED + "",
                "Argument",
                "'" + str + "'",
                message,
                "(Must be Integer between -2,147,483,647 - 2,147,483,647)")));
    return OptionalLong.empty();
  }

  public static Optional<int[]> checkDimensionBoundaries(
      @NotNull final CommandSender sender, final String str) {
    final String[] dims = str.split(":");
    String message = "";
    final int width = ChatUtilities.checkIntegerValidity(dims[0]);
    final int height = ChatUtilities.checkIntegerValidity(dims[1]);
    if (width == Integer.MIN_VALUE) {
      message = dims[0];
    } else if (height == Integer.MIN_VALUE) {
      message = dims[1];
    } else {
      return Optional.of(new int[] {width, height});
    }
    sender.sendMessage(
        ChatUtilities.formatMessage(
            String.join(
                " ",
                ChatColor.RED + "",
                "Argument",
                "'" + message + "'",
                "is not a valid argument!",
                "(Must be Integer)")));
    return Optional.empty();
  }

  public static long checkLongValidity(final String num) {
    try {
      return Long.parseLong(num);
    } catch (final NumberFormatException e) {
      return Long.MIN_VALUE;
    }
  }

  public static int checkIntegerValidity(final String num) {
    try {
      return Integer.parseInt(num);
    } catch (final NumberFormatException e) {
      return Integer.MIN_VALUE;
    }
  }
}
