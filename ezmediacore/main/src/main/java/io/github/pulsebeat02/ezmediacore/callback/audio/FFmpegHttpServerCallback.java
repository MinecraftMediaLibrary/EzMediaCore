/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.executor.ExecutorProvider;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegPlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.RTSPFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.TcpFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.rtp.RTPStreamingServer;
import io.github.pulsebeat02.ezmediacore.utility.network.NetworkUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class FFmpegHttpServerCallback extends ServerCallback {

  private static final String RTSP_SERVER_PATH;

  static {
    RTSP_SERVER_PATH = "live.stream";
  }

  private final RTPStreamingServer server;
  private final int port;

  FFmpegHttpServerCallback(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    super(core, host, port);
    this.server = RTPStreamingServer.ofRtpServer(core, host, port);
    this.port = NetworkUtils.getFreePort();
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    this.startServer(status);
    this.setOutput(player);
  }

  private void setOutput(@NotNull final VideoPlayer player) {
    final FFmpegMediaPlayer ffmpeg = (FFmpegMediaPlayer) player;
    final RTSPFFmpegOutput std = this.getRTSPFFmpegOutput();
    final TcpFFmpegOutput tcp = this.getTcpFFmpegOutput();
    final FFmpegPlayerOutput output = FFmpegPlayerOutput.of(tcp, std);
    ffmpeg.setOutput(output);
  }

  private void startServer(@NotNull final PlayerControls status) {
    if (status == PlayerControls.START || status == PlayerControls.RESUME) {
      this.server.executeAsync(ExecutorProvider.RTSP_SERVER);
    } else {
      this.server.close();
    }
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
    final int port = 8544;
    rtsp.setOutput(ServerOutput.ofHost(host, port, RTSP_SERVER_PATH));
    rtsp.setProperty("f", "rtsp");
    return rtsp;
  }

  @Override
  public void process(final byte @NotNull [] data) {}

  public static final class Builder extends ServerCallback.Builder {

    @Contract("_ -> new")
    @Override
    public @NotNull AudioOutput build(@NotNull final MediaLibraryCore core) {
      return new FFmpegHttpServerCallback(core, this.getHost(), this.getPort());
    }
  }
}
