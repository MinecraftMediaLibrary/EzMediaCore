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
import org.jetbrains.annotations.NotNull;

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
}
