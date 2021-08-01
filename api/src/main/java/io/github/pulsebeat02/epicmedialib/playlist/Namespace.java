package io.github.pulsebeat02.epicmedialib.playlist;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Namespace {

  @NotNull
  String getName();
}
