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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class ChatUtilities {

  private static final ComponentLike PREFIX;

  static {
    PREFIX =
        Component.text()
            .color(NamedTextColor.AQUA)
            .append(
                Component.text('['),
                Component.text("DeluxeMediaPlugin", NamedTextColor.GOLD),
                Component.text(']'));
  }

  @Deprecated
  public static Component formatMessage(@NotNull final String message) {
    return TextComponent.ofChildren(PREFIX, Component.space(), Component.text(message));
  }

  public static Component formatMessage(@NotNull final TextComponent message) {
    return TextComponent.ofChildren(PREFIX, Component.space(), message);
  }

  public static OptionalLong checkMapBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String message;
    final OptionalLong opt = checkLongValidity(str);
    if (!opt.isPresent()) {
      message = "is not a valid argument!";
    } else {
      final long id = opt.getAsLong();
      if (id < -2_147_483_647L) {
        message = "is too low!";
      } else if (id > 2_147_483_647L) {
        message = "is too high!";
      } else {
        return OptionalLong.of(id);
      }
    }
    sender.sendMessage(
        Component.text()
            .color(NamedTextColor.RED)
            .append(
                Component.text("Argument '"),
                Component.text(str, NamedTextColor.GOLD),
                Component.text("' "),
                Component.text(message),
                Component.text(" (Must be Integer between -2,147,483,647 - 2,147,483,647)")));
    return OptionalLong.empty();
  }

  public static Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String[] dims = str.split(":");
    final String message;
    final OptionalInt width = ChatUtilities.checkIntegerValidity(dims[0]);
    final OptionalInt height = ChatUtilities.checkIntegerValidity(dims[1]);
    if (!width.isPresent()) {
      message = dims[0];
    } else if (!height.isPresent()) {
      message = dims[1];
    } else {
      return Optional.of(new int[] {width.getAsInt(), height.getAsInt()});
    }
    sender.sendMessage(
        Component.text()
            .color(NamedTextColor.RED)
            .append(Component.text("Argument '"))
            .append(Component.text(str, NamedTextColor.GOLD))
            .append(Component.text("' "))
            .append(Component.text(message))
            .append(Component.text(" is not a valid argument!"))
            .append(Component.text(" (Must be Integer)")));
    return Optional.empty();
  }

  public static OptionalLong checkLongValidity(@NotNull final String num) {
    try {
      return OptionalLong.of(Long.parseLong(num));
    } catch (final NumberFormatException e) {
      return OptionalLong.empty();
    }
  }

  public static OptionalInt checkIntegerValidity(@NotNull final String num) {
    try {
      return OptionalInt.of(Integer.parseInt(num));
    } catch (final NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  public static TextComponent getCommandUsage(@NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder =
        Component.text().append(Component.text("------------------", NamedTextColor.AQUA));
    for (final Map.Entry<String, String> entry : usages.entrySet()) {
      builder.append(
          Component.join(
              Component.newline(),
              Component.text(entry.getKey(), NamedTextColor.LIGHT_PURPLE),
              Component.text(" - ", NamedTextColor.GOLD),
              Component.text(entry.getValue(), NamedTextColor.AQUA)));
    }
    builder.append(Component.text("------------------", NamedTextColor.AQUA));
    return builder.build();
  }
}
