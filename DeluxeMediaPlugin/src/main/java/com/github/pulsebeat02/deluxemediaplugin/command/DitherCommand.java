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

package com.github.pulsebeat02.deluxemediaplugin.command;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class DitherCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;

  public DitherCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "dither", executor, "deluxemediaplugin.command.dither", "");
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(argument("list", StringArgumentType.word()).executes(this::listSettings));
    literalNode = builder.build();
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }

  @Override
  public String usage() {
    return "/dither list";
  }

  private int listSettings(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    sender.sendMessage(ChatColor.GOLD + "List of Possible Options:");
    for (final DitherSetting setting : DitherSetting.values()) {
      sender.sendMessage(ChatColor.AQUA + setting.name());
    }
    return 1;
  }
}
