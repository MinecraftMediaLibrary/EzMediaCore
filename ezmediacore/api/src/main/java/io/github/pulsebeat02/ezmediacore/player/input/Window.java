package io.github.pulsebeat02.ezmediacore.player.input;

import org.jetbrains.annotations.NotNull;

public interface Window {

  @NotNull
  String getFilePath();

  int getWidth();

  int getHeight();

  int getX();

  int getY();

  @NotNull
  String getTitle();
}
