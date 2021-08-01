package io.github.pulsebeat02.ezmediacore.throwable;

public final class InvalidPackFormatException extends LibraryException {

  private static final long serialVersionUID = -4686809703553076358L;

  public InvalidPackFormatException(final int id) {
    super(String.format("Pack format %d is not a valid pack format!", id));
  }
}
