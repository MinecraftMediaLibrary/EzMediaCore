package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import com.github.pulsebeat02.minecraftmedialibrary.utility.ExtractorUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.AbstractDitherHolder;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.video.itemframe.ItemFrameCallback;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.AbstractVideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.BasicVideoPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.video.player.VLCJIntegratedPlayer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VideoCommand extends AbstractCommand implements CommandExecutor {

    private AbstractDitherHolder dither;
    private AbstractVideoPlayer player;

    // check if user is using youtube
    private boolean youtube;

    // for youtube extraction
    private YoutubeExtraction extractor;

    // for normal file
    private File file;

    // frame dimensions
    private int frameWidth;
    private int frameHeight;

    // int starting map
    private long startingMap;

    public VideoCommand(@NotNull final DeluxeMediaPlugin plugin) {
        super(plugin);
        this.dither = DitherSetting.SIERRA_FILTER_LITE_DITHER.getHolder();
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "You must be a player to use this command!"));
            return true;
        }
        if (args.length == 0) {
            if (player == null) {
                sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "There isn't a video currently playing!"));
            } else {
                if (youtube) {
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
                    sender.sendMessage(ChatColor.GOLD + "Title: " + extractor.getVideoTitle());
                    sender.sendMessage(ChatColor.GOLD + "Author: " + extractor.getAuthor());
                    sender.sendMessage(ChatColor.GOLD + "Rating: " + extractor.getVideoRating());
                    sender.sendMessage(ChatColor.GOLD + "Video Identifier: " + ChatColor.RED + extractor.getVideoId());
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
                } else {
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
                    sender.sendMessage(ChatColor.GOLD + "Video Name: " + file.getName());
                    sender.sendMessage(ChatColor.GOLD + "Size: " + file.getTotalSpace() / 1024 + " Kilobytes");
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.AQUA + "====================================="));
                }
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                if (youtube) {
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Starting Video on URL: " + extractor.getUrl()));
                } else {
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Starting Video on File: " + file.getName()));
                }
                final MinecraftMediaLibrary library = getPlugin().getLibrary();
                final ItemFrameCallback callback = new ItemFrameCallback(getPlugin().getLibrary(), null, startingMap, frameWidth, frameHeight, player.getWidth(), 0, dither);
                if (getPlugin().getLibrary().isUsingVLCJ()) {
                    if (file == null) {
                        player = new VLCJIntegratedPlayer(library,
                                extractor.getUrl(),
                                player.getWidth(),
                                player.getHeight(),
                                callback::send);
                    } else {
                        player = new VLCJIntegratedPlayer(library,
                                file,
                                player.getWidth(),
                                player.getHeight(),
                                callback::send);
                    }
                } else {
                    if (file == null) {
                        player = new BasicVideoPlayer(library,
                                extractor.getUrl(),
                                player.getWidth(),
                                player.getHeight(),
                                callback::send);
                    } else {
                        player = new BasicVideoPlayer(library,
                                file,
                                player.getWidth(),
                                player.getHeight(),
                                callback::send);
                    }
                }
                player.start();
            } else if (args[0].equalsIgnoreCase("stop")) {
                sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Stopped the Video!"));
                player.stop();
            } else {
                sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Param '" + args[0] + "' is not a valid argument!"));
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("load")) {
                final String mrl = args[1];
                final String folderPath = getPlugin().getDataFolder().getAbsolutePath();
                if (ExtractorUtilities.getVideoID(mrl) == null) {
                    final File f = new File(folderPath, mrl);
                    if (f.exists()) {
                        youtube = false;
                        extractor = null;
                        file = f;
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully loaded video " + f.getName()));
                    } else if (mrl.startsWith("http://") || mrl.startsWith("https://")) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Link " + mrl + " is not a valid Youtube video link!"));
                    } else {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "File " + f.getName() + " cannot be found!"));
                    }
                } else {
                    youtube = true;
                    extractor = new YoutubeExtraction(mrl, folderPath);
                    file = null;
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully loaded video " + mrl));
                }
                addHistoryEntry(mrl);
            } else {
                sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Param '" + args[0] + "' is not a valid argument!"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("screen-dimension")) {
                    final String[] dimensions = args[2].split(":");
                    final int width;
                    try {
                        width = Integer.parseInt(dimensions[0]);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + dimensions[0] + "' isn't a valid width! (Must be Integer)"));
                        return true;
                    }
                    final int height;
                    try {
                        height = Integer.parseInt(dimensions[1]);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + dimensions[0] + "' isn't a valid height! (Must be Integer)"));
                        return true;
                    }
                    player.setHeight(height);
                    player.setWidth(width);
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Set dimensions to " + width + ":" + height + " (width:height)"));
                } else if (args[1].equalsIgnoreCase("itemframe-dimension")) {
                    final String[] dimensions = args[2].split(":");
                    final int width;
                    try {
                        width = Integer.parseInt(dimensions[0]);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + dimensions[0] + "' isn't a valid width! (Must be Integer)"));
                        return true;
                    }
                    final int height;
                    try {
                        height = Integer.parseInt(dimensions[1]);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + dimensions[0] + "' isn't a valid height! (Must be Integer)"));
                        return true;
                    }
                    frameWidth = width;
                    frameHeight = height;
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Set itemframe map dimensions to " + frameWidth + ":" + frameHeight + " (width:height)"));
                } else if (args[1].equalsIgnoreCase("starting-map")) {
                    final long id;
                    try {
                        id = Long.parseLong(args[2]);
                    } catch (final NumberFormatException e) {
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
                    startingMap = id;
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Set starting-map on id " + startingMap));
                } else if (args[1].equalsIgnoreCase("dither")) {
                    final String type = args[2];
                    boolean found = false;
                    for (final DitherSetting setting : DitherSetting.values()) {
                        if (setting.name().equalsIgnoreCase(type)) {
                            dither = setting.getHolder();
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Set dither type to " + ChatColor.AQUA + type));
                    } else {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Could not find dither type " + type));
                    }
                } else {
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Param '" + args[0] + "' is not a valid argument!"));
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "load", "set");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                return Arrays.asList("screen-dimension", "itemframe-dimension", "starting-map", "dither");
            } else if (args[0].equalsIgnoreCase("load")) {
                return Collections.singletonList("[Youtube Link or Video File Here]");
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("screen-dimension")) {
                return Collections.singletonList("[Width:Height]");
            } else if (args[1].equalsIgnoreCase("itemframe-dimension")) {
                return Collections.singletonList("[Width:Height]");
            } else if (args[1].equalsIgnoreCase("starting-map")) {
                return Collections.singletonList("[Map ID]");
            } else if (args[1].equalsIgnoreCase("dither")) {
                return Arrays.stream(DitherSetting.values()).map(DitherSetting::name).collect(Collectors.toList());
            }
        }
        return null;
    }

}
