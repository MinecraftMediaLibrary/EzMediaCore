package io.github.pulsebeat02.deluxemediaplugin.utility.nullability;

import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.locale.Locale;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArgumentUtils {

  private ArgumentUtils() {}

  public static boolean requiresPlayer(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final CommandSender sender) {
    return handleFalse(
        plugin.audience().sender(sender), Locale.INVALID_SENDER.build(), sender instanceof Player);
  }

  public static <T> boolean handleEmptyOptional(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NotNull final Optional<T> optional) {
    if (optional.isEmpty()) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }

  public static boolean handleNull(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @Nullable final Object obj) {
    return handleTrue(audience, component, obj == null);
  }

  public static boolean handleNonNull(
      @NotNull final Audience audience,
      @NotNull final Component component,
      @Nullable final Object obj) {
    return handleFalse(audience, component, obj == null);
  }

  public static boolean handleFalse(
      @NotNull final Audience audience,
      @NotNull final Component component,
      final boolean statement) {
    if (!statement) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }

  public static boolean handleTrue(
      @NotNull final Audience audience,
      @NotNull final Component component,
      final boolean statement) {
    if (statement) {
      audience.sendMessage(component);
      return true;
    }
    return false;
  }
}
