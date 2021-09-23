package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public class InvalidMRLException extends LibraryException {

  @Serial private static final long serialVersionUID = 6350588543681340867L;

  public InvalidMRLException(@NotNull final String mrl) {
    super("Invalid MRL %s".formatted(mrl));
  }
}
