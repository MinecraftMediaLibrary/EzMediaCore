/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/28/2021
 * ============================================================================
 */

package com.github.pulsebeat02.deluxemediaplugin.command.rework;

import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ImageCommand extends BaseCommand {

    private final Set<MapImage> images;
    private final Set<UUID> listen;
    private final LiteralCommandNode<CommandSender> literalNode;

    public ImageCommand(@NotNull final TabExecutor executor) {
        super("dither", executor, "deluxemediaplugin.command.image", "");
        this.listen = new HashSet<>();
        this.images = new HashSet<>();
        final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
        builder
                .requires(super::testPermission)
                .then(literal("reset")
                        .then(argument("map", StringArgumentType.word()))
                            .then(argument("id", StringArgumentType.word()))
                        .then(argument("all", StringArgumentType.word()))
                            .executes(this::resetAllMaps))
                .then(literal("set"))
                        .executes()

        literalNode = builder.build();
    }

    @Override
    public LiteralCommandNode<CommandSender> getCommandNode() {
        return literalNode;
    }

    @Override
    public String usage() {
        return "";
    }

    private int resetMap(@NotNull final CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSource();
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
            return 1;
        }
        if (id < 0L) {
            sender.sendMessage(
                    ChatUtilities.formatMessage(
                            ChatColor.RED
                                    + "Argument '"
                                    + args[2]
                                    + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
            return 1;
        } else if (id > 4294967296L) {
            sender.sendMessage(
                    ChatUtilities.formatMessage(
                            ChatColor.RED
                                    + "Argument '"
                                    + args[2]
                                    + "' is too hight! (Must be Integer between 0 - 4,294,967,296)"));
            return 1;
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
        return 1;
    }

    private int resetAllMaps(@NotNull final CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSource();
        listen.add(((Player) sender).getUniqueId());
        sender.sendMessage(
                ChatUtilities.formatMessage(
                        ChatColor.RED
                                + "Are you sure you want to purge and delete all maps? Type YES if you would like to continue."));
        return 1;
    }

}
