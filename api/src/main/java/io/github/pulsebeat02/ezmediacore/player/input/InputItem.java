package io.github.pulsebeat02.ezmediacore.player.input;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InputItem {

  InputItem EMPTY_INPUT = () -> "";

  @NotNull String getInput();

  default void setupInput() {
  }
}
