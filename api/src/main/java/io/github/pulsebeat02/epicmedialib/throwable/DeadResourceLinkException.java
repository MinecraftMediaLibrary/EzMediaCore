package io.github.pulsebeat02.epicmedialib.throwable;

import org.jetbrains.annotations.NotNull;

public final class DeadResourceLinkException extends LibraryException {

  private static final long serialVersionUID = -6428433369003844013L;

  public DeadResourceLinkException(@NotNull final String url) {
    super(String.format("Cannot retrieve resource from %s!", url));
  }
}
