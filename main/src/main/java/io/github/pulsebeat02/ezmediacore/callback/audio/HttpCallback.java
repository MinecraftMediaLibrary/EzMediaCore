package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.extraction.AudioConfiguration;
import io.github.pulsebeat02.ezmediacore.ffmpeg.FFmpegMediaStreamer;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.input.Input;
import io.github.pulsebeat02.ezmediacore.request.MediaRequest;
import io.github.pulsebeat02.ezmediacore.utility.media.RequestUtils;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HttpCallback extends SampleCallback implements ServerCallback {

  private FFmpegMediaStreamer server;
  private CompletableFuture<Void> future;

  private final AudioConfiguration configuration;
  private final String host;
  private final int port;

  public HttpCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final AudioConfiguration configuration,
      @NotNull final Viewers viewers,
      @NotNull final String host,
      final int port) {
    super(core, viewers);
    this.configuration = configuration;
    this.host = host;
    this.port = port;
  }

  @Override
  public void process(final byte @NotNull [] data) {}

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    final Input input = player.getInput().getDirectAudioMrl();
    if (this.server == null) {
      final MediaLibraryCore core = this.getCore();
      final MediaRequest request = RequestUtils.requestMediaInformation(input);
      final String resource = request.getAudioLinks().get(0).getInput();
      this.server =
          FFmpegMediaStreamer.ofFFmpegMediaStreamer(
              core, this.configuration, resource, this.host, this.port);
      this.future = this.server.executeAsync();
    }
  }

  @Override
  public @Nullable CompletableFuture<Void> getServerFuture() {
    return this.future;
  }

  @Override
  public void close() throws Exception {
    if (this.server != null) {
      this.server.close();
    }
    if (this.future != null) {
      this.future.cancel(true);
    }
  }

  public static final class Builder extends AudioCallbackBuilder {

    private AudioConfiguration configuration;
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

    @Contract("_ -> this")
    public Builder configuration(@NotNull final AudioConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    @Override
    public @NotNull AudioCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new HttpCallback(core, this.configuration, viewers, this.host, this.port);
    }
  }
}
