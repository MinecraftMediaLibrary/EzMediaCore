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

package com.github.pulsebeat02.deluxemediaplugin.command.old;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.google.common.primitives.Longs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ImageCommand extends AbstractCommand implements CommandExecutor, Listener {

  private final Set<MapImage> images;
  private final Set<UUID> listen;
  private int width;
  private int height;

  public ImageCommand(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, null, "image", "");
    images = new HashSet<>();
    listen = new HashSet<>();
    width = 1;
    height = 1;
  }

  @Override
  public void performCommandTask(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String s,
      final String[] args) {
    if (args.length == 0) {
      sender.sendMessage(ChatColor.GOLD + "Current Images Loaded");
      sender.sendMessage(
          ChatColor.GREEN + "[Map ID] " + ChatColor.GOLD + ":" + ChatColor.AQUA + " [MRL]");
      for (final MapImage image : images) {
        sender.sendMessage(
            ChatColor.GREEN
                + ""
                + image.getMap()
                + ChatColor.GOLD
                + " : "
                + ChatColor.AQUA
                + image.getImage().getName());
      }
    } else if (args.length == 1) {
      if (args[0].equalsIgnoreCase("rickroll")) {
        final File f =
            FileUtilities.downloadImageFile(
                "https://images.news18.com/ibnlive/uploads/2020/12/1607660925_untitled-design-2020-12-11t095722.206.png",
                getPlugin().getDataFolder().getAbsolutePath());
        new MapImage(getPlugin().getLibrary(), 69, f, width, height).drawImage();
      }
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("reset")) {
        if (args[1].equalsIgnoreCase("all")) {
          listen.add(((Player) sender).getUniqueId());
          sender.sendMessage(
              ChatUtilities.formatMessage(
                  ChatColor.RED
                      + "Are you sure you want to purge and delete all maps? Type YES if you would like to continue."));
        }
      }
    } else if (args.length == 3) {
      if (args[0].equalsIgnoreCase("reset")) {
        if (args[1].equalsIgnoreCase("map")) {
          final long id;
          try {
            id = Long.parseLong(args[2]);
          } catch (final NumberFormatException e) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
            return;
          }
          if (id < 0L) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
            return;
          } else if (id > 4294967296L) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is too hight! (Must be Integer between 0 - 4,294,967,296)"));
            return;
          }
          for (final MapImage image : images) {
            if (image.getMap() == id) {
              images.remove(image);
              break;
            }
          }
          MapImage.resetMap(getPlugin().getLibrary(), (int) id);
          sender.sendMessage(
              ChatUtilities.formatMessage(
                  ChatColor.GOLD + "Successfully purged the map with ID " + id));
        }
      }
    } else if (args.length == 4) {
      if (args[0].equalsIgnoreCase("set")) {
        if (args[1].equalsIgnoreCase("map")) {
          final long id;
          try {
            id = Long.parseLong(args[2]);
          } catch (final NumberFormatException e) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
            return;
          }
          if (id < -2_147_483_647L) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is too low! (Must be Integer between -2,147,483,647 - 2,147,483,647)"));
            return;
          } else if (id > 2_147_483_647L) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is too high! (Must be Integer between -2,147,483,647 - 2,147,483,647)"));
            return;
          }
          final String mrl = args[3];
          // if it's between the bounds, it is safe to cast to integer because the original value is
          // still being stored, and there is no integer overflow
          if ((mrl.startsWith("http://")) || mrl.startsWith("https://") && mrl.endsWith(".png")) {
            final File img =
                FileUtilities.downloadImageFile(mrl, getPlugin().getDataFolder().getAbsolutePath());
            new MapImage(getPlugin().getLibrary(), (int) id, img, width, height).drawImage();
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.GOLD + "Successfully drew image on map " + id));
          } else {
            final File f = new File(getPlugin().getDataFolder(), mrl);
            if (f.exists()) {
              new MapImage(getPlugin().getLibrary(), (int) id, f, width, height).drawImage();
              sender.sendMessage(
                  ChatUtilities.formatMessage(
                      ChatColor.GOLD + "Successfully drew image on map " + id));
            } else {
              sender.sendMessage(
                  ChatUtilities.formatMessage(
                      ChatColor.RED + "File " + f.getName() + " cannot be found!"));
            }
          }
        } else if (args[1].equalsIgnoreCase("dimensions")) {
          final int w;
          try {
            w = Integer.parseInt(args[2]);
          } catch (final NumberFormatException e) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is not a valid argument! (Must be Integer)"));
            return;
          }
          final int h;
          try {
            h = Integer.parseInt(args[2]);
          } catch (final NumberFormatException e) {
            sender.sendMessage(
                ChatUtilities.formatMessage(
                    ChatColor.RED
                        + "Argument '"
                        + args[2]
                        + "' is not a valid argument! (Must be Integer)"));
            return;
          }
          width = w;
          height = h;
          sender.sendMessage(
              ChatUtilities.formatMessage(
                  ChatColor.GOLD
                      + "Changed item frame dimensions to "
                      + width
                      + ":"
                      + height
                      + " (width:height"));
        }
      }
    }
  }

  @EventHandler
  public void onPlayerChat(final AsyncPlayerChatEvent event) {
    final Player p = event.getPlayer();
    if (listen.contains(p.getUniqueId())) {
      if (event.getMessage().equalsIgnoreCase("YES")) {
        for (final MapImage image : images) {
          MapImage.resetMap(getPlugin().getLibrary(), image.getMap());
        }
        images.clear();
        p.sendMessage(
            ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged all image maps"));
      } else {
        p.sendMessage(
            ChatUtilities.formatMessage(ChatColor.GOLD + "Cancelled purge of all image maps"));
      }
    }
  }

  @Override
  public List<String> onTabComplete(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String s,
      final String[] args) {
    if (args.length == 1) {
      return Arrays.asList("rickroll", "reset", "set");
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("reset")) {
        return Arrays.asList("all", "map");
      } else if (args[0].equalsIgnoreCase("set")) {
        return Arrays.asList("map", "dimensions");
      }
    } else if (args.length == 3) {
      if (args[1].equalsIgnoreCase("map")) {
        if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("set")) {
          return Collections.singletonList("[Map ID]");
        }
      } else if (args[1].equalsIgnoreCase("dimensions")) {
        return Collections.singletonList("[Width:Height]");
      }
    } else if (args.length == 4) {
      if (args[0].equalsIgnoreCase("set")) {
        if (args[1].equalsIgnoreCase("map")) {
          return Collections.singletonList("[PNG URL Image or File]");
        }
      }
    }
    return null;
  }
}
