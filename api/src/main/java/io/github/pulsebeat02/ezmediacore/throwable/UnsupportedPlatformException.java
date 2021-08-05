package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.jetbrains.annotations.NotNull;

public final class UnsupportedPlatformException extends LibraryException {

  @Serial
  private static final long serialVersionUID = 1682368011870345698L;

  public UnsupportedPlatformException(@NotNull final String platform) {
    super("Platform %s is not supported by EzMediaCore!".formatted(platform));
  }
}
