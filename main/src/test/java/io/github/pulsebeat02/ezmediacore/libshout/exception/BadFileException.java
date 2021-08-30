package io.github.pulsebeat02.ezmediacore.libshout.exception;

import java.io.Serial;

/**
 * bad source file
 *
 * @author caorong
 */
public class BadFileException extends Exception {

  @Serial
  private static final long serialVersionUID = -8935110974026120897L;

  public BadFileException() {
  }

  public BadFileException(final String message) {
    super(message);
  }

  public BadFileException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public BadFileException(final Throwable cause) {
    super(cause);
  }
}
