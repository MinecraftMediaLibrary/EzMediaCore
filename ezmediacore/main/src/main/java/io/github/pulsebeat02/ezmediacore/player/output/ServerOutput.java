package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ServerOutput implements OutputHandler<String> {

  private final String ip;

  ServerOutput(final String host, final int port, @NotNull final String... path) {
    this.ip = "%s:%s/%s".formatted(host, port, String.join("/", path));
  }

  @Contract("_, _, _ -> new")
  public static @NotNull ServerOutput ofHost(
      @NotNull final String host, final int port, @NotNull final String... path) {
    return new ServerOutput(host, port, path);
  }

  @Override
  public @NotNull String getRaw() {
    return this.ip;
  }
}
