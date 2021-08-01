package io.github.pulsebeat02.epicmedialib.throwable;

import org.jetbrains.annotations.NotNull;

public final class HttpFailureException extends LibraryException {

  private static final long serialVersionUID = -8317098640373685160L;

  public HttpFailureException(@NotNull final String message) {
    super(String.format("HTTP Exception: %s", message));
  }
}
