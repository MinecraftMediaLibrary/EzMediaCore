package io.github.pulsebeat02.ezmediacore.callback.rewrite.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.ServerCallbackBase;
import io.github.pulsebeat02.ezmediacore.callback.rewrite.AudioHandler;
import org.jetbrains.annotations.NotNull;

public abstract class ServerCallback extends AudioHandler implements ServerCallbackBase {

  private final String host;
  private final int port;
  public ServerCallback(@NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
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
}
