package com.github.pulsebeat02.utility;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ChatUtilities {

    public static String formatMessage(@NotNull final String message) {
        return ChatColor.AQUA + "["
                + ChatColor.GOLD + "DeluxeMediaPlugin"
                + ChatColor.AQUA + "] "
                + message;
    }

}
