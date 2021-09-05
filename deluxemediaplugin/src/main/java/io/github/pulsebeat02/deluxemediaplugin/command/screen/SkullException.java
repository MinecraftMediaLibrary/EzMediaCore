package io.github.pulsebeat02.deluxemediaplugin.command.screen;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public class SkullException extends AssertionError {

  @Serial private static final long serialVersionUID = 2394089956129784304L;

  public SkullException(@NotNull final String message) {
    super("Invalid Skull Base64 %s!".formatted(message));
  }

  @Override
  public synchronized Throwable getCause() {
    return this;
  }

  @Override
  public synchronized Throwable initCause(@NotNull final Throwable cause) {
    return this;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
