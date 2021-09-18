package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.player.MrlConfiguration;
import org.jcodec.common.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class ArgumentUtils {

  private ArgumentUtils() {}

  public static MrlConfiguration checkPlayerArguments(@NotNull final Object @NotNull [] arguments) {
    Preconditions.checkArgument(arguments != null, "Arguments cannot be null!");
    Preconditions.checkArgument(
        arguments.length > 0, "Invalid argument length! Must have at least 1!");
    final Object mrl = arguments[0];
    Preconditions.checkArgument(
        mrl instanceof MrlConfiguration, "Invalid MRL type! Must be a MrlConfiguration!");
    return (MrlConfiguration) mrl;
  }
}
