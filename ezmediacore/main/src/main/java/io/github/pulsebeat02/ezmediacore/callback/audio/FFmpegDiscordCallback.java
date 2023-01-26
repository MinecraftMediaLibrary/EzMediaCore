/*
 * MIT License
 *
 * Copyright (c) 2023 Brandon Li
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
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.buffered.FFmpegMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.ServerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.FFmpegPlayerOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.StdoutFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ffmpeg.TcpFFmpegOutput;
import io.github.pulsebeat02.ezmediacore.utility.network.NetworkUtils;
import org.jetbrains.annotations.Contract;
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
    this.startServer(player);
  }

  private void startServer(@NotNull final VideoPlayer player) {
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

  public static final class Builder extends AudioOutputBuilder {

    @Contract("_ -> new")
    @Override
    public @NotNull AudioOutput build(@NotNull final MediaLibraryCore core) {
      return new FFmpegDiscordCallback(core);
    }
  }
}
