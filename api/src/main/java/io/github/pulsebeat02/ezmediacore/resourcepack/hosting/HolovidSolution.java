package io.github.pulsebeat02.ezmediacore.resourcepack.hosting;

import org.jetbrains.annotations.NotNull;

public interface HolovidSolution extends HostingSolution {

  @Override
  @NotNull
  default String getName() {
    return "Holovid Resourcepack Integrated Server";
  }
}
