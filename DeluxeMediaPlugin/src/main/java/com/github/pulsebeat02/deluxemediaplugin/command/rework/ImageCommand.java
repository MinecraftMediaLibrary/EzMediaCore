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

import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class ImageCommand extends BaseCommand {

    private final LiteralCommandNode<CommandSender> literalNode;

    public ImageCommand(@NotNull final TabExecutor executor) {
        super("dither", executor, "deluxemediaplugin.command.image", "");
        final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
        builder
                .requires(super::testPermission)
                .then(argument("resetMap", StringArgumentType.word())
                        .executes(this::resetMap));

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
        return 1;
    }

}
