package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class UnknownArtistException extends LibraryException {
  @Serial
  private static final long serialVersionUID = 888269687137805842L;

  public UnknownArtistException(@NotNull final String url) {
    super("Cannot retrieve artist from %s!".formatted(url));
  }
}
