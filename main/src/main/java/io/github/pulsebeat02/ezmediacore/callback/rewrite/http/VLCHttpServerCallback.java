package io.github.pulsebeat02.ezmediacore.callback.rewrite.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.TcpOutput;
import io.github.pulsebeat02.ezmediacore.resourcepack.hosting.HttpServer;
import io.github.pulsebeat02.ezmediacore.utility.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/*
 * VLCHttpServerCallback assumes that the output is already using a TCPOutput (meaning
 * VLC is directly sending the packets to the website), so all we have to do is to
 * prepare the HTTP server for audio.
 */
public final class VLCHttpServerCallback extends ServerCallback {

  private HttpServer server;
  private final String host;
  private final int port;

  VLCHttpServerCallback(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    super(core);
    this.host = host;
    this.port = port;
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    final VLCMediaPlayer vlc = (VLCMediaPlayer) player;
    final TcpOutput output = (TcpOutput) vlc.getOutput().getResultingOutput();
    this.replaceOccurrence(output, status);
    this.setupHttpServer();
  }

  private void replaceOccurrence(
      @NotNull final TcpOutput output, @NotNull final PlayerControls status) {
    if (status == PlayerControls.START) {
      final MediaLibraryCore core = this.getCore();
      final Path html = core.getHttpServerPath().resolve("audio.html");
      final String content =
          FileUtils.getContentExceptionally(html).replaceAll("%%HOST_ADDRESS%%", output.getRaw());
      FileUtils.setContentExceptionally(html, content);
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

  @Override
  public void process(final byte @NotNull [] data) {}

}
