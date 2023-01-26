package io.github.pulsebeat02.ezmediacore.callback.rewrite.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegPlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.RTSPFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.TcpFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.utility.network.NetworkUtils;
import org.jetbrains.annotations.NotNull;

public final class FFmpegHttpServerCallback extends ServerCallback {

  private static final String RTSP_SERVER_PATH;

  static {
    RTSP_SERVER_PATH = "live.stream";
  }

  private final int port;

  FFmpegHttpServerCallback(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    super(core, host, port);
    this.port = NetworkUtils.getFreePort();
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    final FFmpegMediaPlayer ffmpeg = (FFmpegMediaPlayer) player;
    final RTSPFFmpegOutput std = this.getRTSPFFmpegOutput();
    final TcpFFmpegOutput tcp = this.getTcpFFmpegOutput();
    final FFmpegPlayerOutput output = FFmpegPlayerOutput.of(tcp, std);
    ffmpeg.setOutput(output);
  }

  @NotNull
  private TcpFFmpegOutput getTcpFFmpegOutput() {
    final TcpFFmpegOutput tcp = TcpFFmpegOutput.ofOutput();
    final String host = "tcp://localhost";
    tcp.setOutput(ServerOutput.ofHost(host, this.port));
    tcp.setProperty("f", "nut");
    return tcp;
  }

  @NotNull
  private RTSPFFmpegOutput getRTSPFFmpegOutput() {
    final RTSPFFmpegOutput rtsp = RTSPFFmpegOutput.ofOutput();
    final String host = "rtsp://%s".formatted(this.getHost());
    final int port = this.getPort();
    rtsp.setOutput(ServerOutput.ofHost(host, port, RTSP_SERVER_PATH));
    rtsp.setProperty("f", "rtsp");
    return rtsp;
  }

  @Override
  public void process(final byte @NotNull [] data) {}
}
