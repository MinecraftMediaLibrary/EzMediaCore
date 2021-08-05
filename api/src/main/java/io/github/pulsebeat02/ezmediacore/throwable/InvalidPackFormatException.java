package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;

public final class InvalidPackFormatException extends LibraryException {

  @Serial
  private static final long serialVersionUID = -4686809703553076358L;

  public InvalidPackFormatException(final int id) {
    super("Pack format %d is not a valid pack format!".formatted(id));
  }
}
