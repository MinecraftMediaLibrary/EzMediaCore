package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.Viewers;
import io.github.pulsebeat02.ezmediacore.dither.buffer.ByteBufCarrier;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.TcpOutput;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class DiscordCallback extends DataCallback {

  private final DiscordAudioConsumer consumer;

  private final TcpOutput output; // server for vlc to play audio to (no port-forward)

  private HttpServer server; // http server to host the html file (port-forward)
  private final String host;
  private final int port;

  public DiscordCallback(
      @NotNull final MediaLibraryCore core,
      @NotNull final Viewers viewers,
      @NotNull final DiscordAudioConsumer consumer,
      @NotNull final TcpOutput output,
      @NotNull final String host,
      final int port) {
    super(core, viewers);
    this.consumer = consumer;
    this.output = output;
    this.host = host;
    this.port = port;
  }

  @Override
  public void process(final byte @NotNull [] data) {
    this.consumer.consume(ByteBufCarrier.ofByteArray(data));
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer<?> player, @NotNull final PlayerControls status) {
    if (player instanceof VLCMediaPlayer) {
      this.replaceOccurrence();
      this.setupHttpServer();
    }
  }

  private void setupHttpServer() {
    try {
      final MediaLibraryCore core = this.getCore();
      if (this.server == null) {
        this.server = HttpServer.ofServer(core, this.host, this.port, true);
        this.server.startServer();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void replaceOccurrence() {
    final MediaLibraryCore core = this.getCore();
    final Path html = core.getHttpServerPath().resolve("audio.html");
    final String content =
        FileUtils.getContentExceptionally(html)
            .replaceAll("%%HOST_ADDRESS%%", this.output.getRaw());
    FileUtils.setContentExceptionally(html, content);
  }

  @Override
  public void close() throws Exception {
    if (this.server != null) {
      this.server.stopServer();
    }
  }

  public static final class Builder extends AudioCallbackBuilder {

    private DiscordAudioConsumer consumer;
    private TcpOutput output;
    private String host;
    private int port;

    Builder() {}

    @Contract("_ -> this")
    public Builder consumer(@NotNull final DiscordAudioConsumer consumer) {
      this.consumer = consumer;
      return this;
    }

    @Contract("_ -> this")
    public TcpOutput output(@NotNull final TcpOutput output) {
      this.output = output;
      return this.output;
    }

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
    public @NotNull DiscordCallback build(@NotNull final MediaLibraryCore core) {
      final Viewers viewers = this.getViewers();
      return new DiscordCallback(core, viewers, this.consumer, this.output, this.host, this.port);
    }
  }
}
