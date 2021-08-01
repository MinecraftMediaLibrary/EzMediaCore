package io.github.pulsebeat02.epicmedialib.throwable;

import org.jetbrains.annotations.NotNull;

public final class UnsupportedPlatformException extends LibraryException {

  private static final long serialVersionUID = 1682368011870345698L;

  public UnsupportedPlatformException(@NotNull final String platform) {
    super(String.format("Platform %s is not supported by EpicMediaLib!", platform));
  }
}
