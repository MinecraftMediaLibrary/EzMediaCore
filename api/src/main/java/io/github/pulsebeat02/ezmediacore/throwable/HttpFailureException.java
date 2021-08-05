package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class HttpFailureException extends LibraryException {

  @Serial
  private static final long serialVersionUID = -8317098640373685160L;

  public HttpFailureException(@NotNull final String message) {
    super("HTTP Exception: %s".formatted(message));
  }
}
