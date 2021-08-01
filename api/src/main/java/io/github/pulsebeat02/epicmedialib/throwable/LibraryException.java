package io.github.pulsebeat02.epicmedialib.throwable;

import org.jetbrains.annotations.NotNull;

public class LibraryException extends AssertionError {

  private static final long serialVersionUID = -6914539609629173333L;

  public LibraryException(@NotNull final String message) {
    super(message);
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
