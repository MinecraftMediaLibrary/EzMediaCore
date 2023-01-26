package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.FFmpegJDADiscordCallback;
import io.github.pulsebeat02.ezmediacore.callback.audio.JDAAudioStream;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegPlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.StdoutFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.TcpFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.utility.network.NetworkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

/*
 * FFmpeg audio stream from stdout is exposed to user, so they can read from it and
 * play it into the Discord bot.
 */
public final class FFmpegDiscordCallback extends DiscordCallback
    implements FFmpegJDADiscordCallback {

  private JDAAudioStream stream;

  private final int port;

  FFmpegDiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
    this.port = NetworkUtils.getFreePort();
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {

    final FFmpegMediaPlayer ffmpeg = (FFmpegMediaPlayer) player;
    final StdoutFFmpegOutput std = this.getStdoutFFmpegOutput();
    final TcpFFmpegOutput tcp = this.getTcpFFmpegOutput();
    final FFmpegPlayerOutput output = FFmpegPlayerOutput.of(tcp, std);
    ffmpeg.setOutput(output);

    final InputStream input = std.getResultingOutput().getRaw();
    this.setOutput(input);
  }

  private void setOutput(@NotNull final InputStream input) {
    try {
      this.stream = new JDAAudioPlayerStreamSendHandler(AudioSystem.getAudioInputStream(input));
    } catch (final UnsupportedAudioFileException | IOException e) {
      throw new AssertionError(e);
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
  private StdoutFFmpegOutput getStdoutFFmpegOutput() {
    final StdoutFFmpegOutput std = StdoutFFmpegOutput.ofOutput();
    std.setProperty("select", "a");
    std.setProperty("f", "wav");
    return std;
  }

  @Override
  public @Nullable JDAAudioStream getStream() {
    return this.stream;
  }

  @Override
  public void process(final byte @NotNull [] data) {}
}
