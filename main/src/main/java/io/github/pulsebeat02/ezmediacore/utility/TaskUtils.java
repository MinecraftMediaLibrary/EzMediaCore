package io.github.pulsebeat02.ezmediacore.utility;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class TaskUtils {

  private TaskUtils() {
  }

  public static <T> Future<T> sync(@NotNull final MediaLibraryCore core,
      @NotNull final Callable<T> task) {
    return Bukkit.getScheduler().callSyncMethod(core.getPlugin(), task);
  }

}
