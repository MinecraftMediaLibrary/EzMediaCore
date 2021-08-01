package io.github.pulsebeat02.ezmediacore.throwable;

import org.jetbrains.annotations.NotNull;

public final class InvalidPackResourceException extends LibraryException {

  private static final long serialVersionUID = 1682368011870345638L;

  public InvalidPackResourceException(@NotNull final String message) {
    super(String.format("Resourcepack Exception: %s", message));
  }
}
