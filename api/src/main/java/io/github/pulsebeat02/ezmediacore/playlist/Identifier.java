package io.github.pulsebeat02.ezmediacore.playlist;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Identifier {

  @NotNull
  String getId();
}
