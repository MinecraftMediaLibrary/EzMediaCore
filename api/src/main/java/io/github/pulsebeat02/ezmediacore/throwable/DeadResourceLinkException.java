package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class DeadResourceLinkException extends LibraryException {

  @Serial
  private static final long serialVersionUID = -6428433369003844013L;

  public DeadResourceLinkException(@NotNull final String url) {
    super("Cannot retrieve resource from %s!".formatted(url));
  }
}
