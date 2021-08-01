package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.listener.ForcefulResourcepackListener;
import io.github.pulsebeat02.ezmediacore.resourcepack.PackFormat;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class ResourcepackUtils {

  private ResourcepackUtils() {}

  public static boolean validatePackFormat(final int format) {
    return Arrays.stream(PackFormat.values()).anyMatch(value -> value.getId() == format);
  }

  public static boolean validateResourcepackIcon(@NotNull final Path icon) {
    return PathUtils.getName(icon).endsWith(".png");
  }

  public static void forceResourcepackLoad(
      @NotNull final Plugin plugin,
      @NotNull final Collection<? extends Player> players,
      @NotNull final String url,
      final byte @NotNull [] hash) {
    new ForcefulResourcepackListener(
            plugin,
            players.stream().map(Player::getUniqueId).collect(Collectors.toSet()),
            url,
            hash)
        .start(plugin);
  }
}
