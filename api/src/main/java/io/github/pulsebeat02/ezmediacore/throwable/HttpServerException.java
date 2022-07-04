package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public class HttpServerException extends LibraryException {

  @Serial
  private static final long serialVersionUID = -1514561393739918028L;

  public HttpServerException(@NotNull final String message) {
    super(message);
  }
}
