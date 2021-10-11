package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;

public class InvalidStreamHeaderException extends LibraryException {

  @Serial
  private static final long serialVersionUID = 977190275072671764L;

  public InvalidStreamHeaderException() {
    super("Invalid stream header count! Cannot be 0!");
  }
}
