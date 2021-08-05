package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class UnknownPlaylistException extends LibraryException {

  @Serial
  private static final long serialVersionUID = 6632975059132666888L;

  public UnknownPlaylistException(final @NotNull String playlist) {
    super("Cannot retrieve information on playlist %s!".formatted(playlist));
  }
}
