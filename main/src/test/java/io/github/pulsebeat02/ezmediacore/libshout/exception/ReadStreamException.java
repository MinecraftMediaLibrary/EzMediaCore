package io.github.pulsebeat02.ezmediacore.libshout.exception;

import java.io.Serial;

/**
 * ex when read stream for push
 *
 * @author caorong
 */
public class ReadStreamException extends RuntimeException {

  @Serial private static final long serialVersionUID = 3531880196344252231L;

  public ReadStreamException() {}

  public ReadStreamException(final int code) {
    super("source stream code is " + code);
  }

  public ReadStreamException(final String message) {
    super(message);
  }

  public ReadStreamException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ReadStreamException(final Throwable cause) {
    super(cause);
  }
}
