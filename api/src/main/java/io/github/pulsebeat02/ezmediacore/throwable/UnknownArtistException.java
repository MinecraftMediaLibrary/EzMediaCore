package io.github.pulsebeat02.ezmediacore.throwable;

import org.jetbrains.annotations.NotNull;

public final class UnknownArtistException extends LibraryException {
  private static final long serialVersionUID = 888269687137805842L;

  public UnknownArtistException(@NotNull final String url) {
    super(String.format("Cannot retrieve artist from %s!", url));
  }
}
