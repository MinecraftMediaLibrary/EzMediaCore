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
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.video.dither.DitherSetting;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class DitherCommand extends BaseCommand {

  private final LiteralCommandNode<CommandSender> literalNode;

  public DitherCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "dither", executor, "deluxemediaplugin.command.dither", "");
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder.requires(super::testPermission).then(literal("list").executes(this::listSettings));
    literalNode = builder.build();
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }

  @Override
  public Component usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.of("/dither list", "Lists all possible dithering algorithms to choose"));
  }

  private int listSettings(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Possible Settings: ", NamedTextColor.GOLD)));
    for (final DitherSetting setting : DitherSetting.values()) {
      audience.sendMessage(Component.text(setting.name(), NamedTextColor.AQUA));
    }
    return 1;
  }
}
