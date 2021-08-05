package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class InvalidPackResourceException extends LibraryException {

  @Serial
  private static final long serialVersionUID = 1682368011870345638L;

  public InvalidPackResourceException(@NotNull final String message) {
    super("Resourcepack Exception: %s".formatted(message));
  }
}
