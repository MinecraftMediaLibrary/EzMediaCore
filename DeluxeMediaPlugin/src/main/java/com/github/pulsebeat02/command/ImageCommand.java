package com.github.pulsebeat02.command;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.image.MapImage;
import com.github.pulsebeat02.utility.ChatUtilities;
import com.github.pulsebeat02.utility.FileUtilities;
import com.github.pulsebeat02.video.dither.DitherSetting;
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
import java.util.stream.Collectors;

public class ImageCommand extends AbstractCommand implements CommandExecutor, Listener {

    private final Set<MapImage> images;
    private final Set<UUID> listen;
    private int width;
    private int height;

    public ImageCommand(@NotNull DeluxeMediaPlugin plugin) {
        super(plugin);
        this.images = new HashSet<>();
        this.listen = new HashSet<>();
        this.width = 1;
        this.height = 1;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "You must be a player to use this command!"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Current Images Loaded");
            sender.sendMessage(ChatColor.GREEN + "[Map ID] " + ChatColor.GOLD + ":" + ChatColor.AQUA + " [MRL]");
            for (MapImage image : images) {
                sender.sendMessage(ChatColor.GREEN + "" + image.getMap() + ChatColor.GOLD + " : " + ChatColor.AQUA + image.getImage().getName());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("rickroll")) {
                File f = FileUtilities.downloadImageFile("https://lh3.googleusercontent.com/proxy/_H3t3D3huCELuETTgKOU5uGrjxSZ4uT2B0Y1wtrvEaA9XByYfg726rzlFh9ppcGHrO-Gv7_nD6Z6DbP5PQWrQ2NGXPns4TUK8xUUkNbJfdJCu2Lwc-31XBa-LDcU", getPlugin().getDataFolder().getAbsolutePath());
                new MapImage(getPlugin().getLibrary(), 69, f, width, height).drawImage();
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("all")) {
                    listen.add(((Player) sender).getUniqueId());
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Are you sure you want to purge and delete all maps? Type YES if you would like to continue."));
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("map")) {
                    long id;
                    try {
                        id = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    if (id < 0L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    } else if (id > 4294967296L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too hight! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    for (MapImage image : images) {
                        if (image.getMap() == id) {
                            images.remove(image);
                            break;
                        }
                    }
                    MapImage.resetMap(getPlugin().getLibrary(), id);
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged the map with ID " + id));
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("map")) {
                    long id;
                    try {
                        id = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    if (id < 0L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    } else if (id > 4294967296L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too high! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    String mrl = args[3];
                    if ((mrl.startsWith("http://")) || mrl.startsWith("https://") && mrl.endsWith(".png")) {
                        File img = FileUtilities.downloadImageFile(mrl, getPlugin().getDataFolder().getAbsolutePath());
                        new MapImage(getPlugin().getLibrary(), id, img, width, height).drawImage();
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully drew image on map " + id));
                    } else {
                        File f = new File(getPlugin().getDataFolder(), mrl);
                        if (f.exists()) {
                            new MapImage(getPlugin().getLibrary(), id, f, width, height).drawImage();
                            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully drew image on map " + id));
                        } else {
                            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "File " + f.getName() + " cannot be found!"));
                        }
                    }
                } else if (args[1].equalsIgnoreCase("dimensions")) {
                    int w;
                    try {
                        w = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer)"));
                        return true;
                    }
                    int h;
                    try {
                        h = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer)"));
                        return true;
                    }
                    width = w;
                    height = h;
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Changed item frame dimensions to " + width + ":" + height + " (width:height"));
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (listen.contains(p.getUniqueId())) {
            if (event.getMessage().equalsIgnoreCase("YES")) {
                for (MapImage image : images) {
                    MapImage.resetMap(getPlugin().getLibrary(), image.getMap());
                }
                images.clear();
                p.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged all image maps"));
            } else {
                p.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Cancelled purge of all image maps"));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 0) {
            return Arrays.asList("rickroll", "reset", "set");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                return Arrays.asList("all", "map");
            } else if (args[0].equalsIgnoreCase("set")) {
                return Arrays.asList("map", "dimensions");
            }
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("map")) {
                if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("set")) {
                    return Collections.singletonList("[Map ID]");
                }
            } else if (args[1].equalsIgnoreCase("dimensions")) {
                return Collections.singletonList("[Width:Height]");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("map")) {
                    return Collections.singletonList("[PNG URL Image or File]");
                }
            }
        }
        return null;
    }

}
