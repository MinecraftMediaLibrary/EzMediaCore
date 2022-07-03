package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.AudioCallback;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class HttpCallback extends SampleCallback {

  private final String ip;

  public HttpCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final String host,
      final int port) {
    super(core, viewers);
    this.ip = "%s:%s".formatted(host, port);
  }

  @Override
  public void process(final byte @NotNull [] data) {

  }

  @Override
  public void preparePlayerStateChange(@NotNull final PlayerControls status) {
  }

  public static final class Builder extends AudioCallbackBuilder {

    private String host;
    private int port;

    Builder() {}

    @Contract("_ -> this")
    public Builder host(@NotNull final String host) {
      this.host = host;
      return this;
    }

    @Contract("_ -> this")
    public Builder port(final int port) {
      this.port = port;
      return this;
    }

    @Override
    public @NotNull AudioCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new HttpCallback(core, viewers, this.host, this.port);
    }
  }
}
