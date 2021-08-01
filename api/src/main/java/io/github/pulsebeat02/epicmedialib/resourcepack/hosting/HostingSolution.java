package io.github.pulsebeat02.epicmedialib.resourcepack.hosting;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HostingSolution {

  @NotNull
  Path createUrl(@NotNull final Path resourcepack);

  @NotNull
  default String getName() {
    return "MML Custom Solution";
  }
}
