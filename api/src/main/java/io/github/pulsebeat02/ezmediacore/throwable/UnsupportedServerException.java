package io.github.pulsebeat02.ezmediacore.throwable;

import java.io.Serial;
import org.bukkit.Bukkit;

public final class UnsupportedServerException extends LibraryException {

  @Serial private static final long serialVersionUID = -8545011101041050211L;

  public UnsupportedServerException() {
    super("Server version %s is not supported by EzMediaCore!".formatted(Bukkit.getVersion()));
  }
}
