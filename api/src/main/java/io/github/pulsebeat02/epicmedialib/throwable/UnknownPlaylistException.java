package io.github.pulsebeat02.epicmedialib.throwable;

import org.jetbrains.annotations.NotNull;

public final class UnknownPlaylistException extends LibraryException {

  private static final long serialVersionUID = 6632975059132666888L;

  public UnknownPlaylistException(final @NotNull String playlist) {
    super(String.format("Cannot retrieve information on playlist %s!", playlist));
  }
}
