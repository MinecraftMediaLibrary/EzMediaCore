package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ServerCallback extends AudioOutput implements ServerCallbackProxy {

  private final String host;
  private final int port;

  public ServerCallback(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    super(core);
    this.host = host;
    this.port = port;
  }

  @Override
  public @NotNull String getHost() {
    return this.host;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  public abstract static sealed class Builder extends AudioCallbackBuilder
      permits FFmpegHttpServerCallback.Builder, VLCHttpServerCallback.Builder {

    private String host;
    private int port;

    @Contract("_ -> this")
    public @NotNull Builder host(@NotNull final String host) {
      this.host = host;
      return this;
    }

    @Contract("_ -> this")
    public @NotNull Builder port(final int port) {
      this.port = port;
      return this;
    }

    protected int getPort() {
      return this.port;
    }

    protected String getHost() {
      return this.host;
    }
  }
}
