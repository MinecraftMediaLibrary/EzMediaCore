package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HostingSolution {

  @NotNull
  String createUrl(@NotNull final Path resourcepack);

  @NotNull
  default String getName() {
    return "EzMediaCore Custom Solution";
  }
}
