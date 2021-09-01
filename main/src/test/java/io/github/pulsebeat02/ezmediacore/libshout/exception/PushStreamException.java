package io.github.pulsebeat02.ezmediacore.libshout.exception;

import java.io.Serial;

/**
 * ex when push stream to icecast server
 *
 * @author caorong
 */
public class PushStreamException extends RuntimeException {

  @Serial private static final long serialVersionUID = 3516759721627203423L;

  public PushStreamException() {}

  public PushStreamException(final String message) {
    super(message);
  }

  public PushStreamException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public PushStreamException(final Throwable cause) {
    super(cause);
  }
}
