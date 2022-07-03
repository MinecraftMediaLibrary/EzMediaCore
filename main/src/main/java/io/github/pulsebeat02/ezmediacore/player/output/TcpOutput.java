package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TcpOutput implements OutputHandler<String> {

  private final String ip;

  TcpOutput(final String host, final int port) {
    this.ip = "%s:%s".formatted(host, port);
  }

  @Contract("_, _ -> new")
  public static @NotNull TcpOutput ofHost(@NotNull final String host, final int port) {
    return new TcpOutput(host, port);
  }

  @Override
  public @NotNull String getRaw() {
    return this.ip;
  }
}
