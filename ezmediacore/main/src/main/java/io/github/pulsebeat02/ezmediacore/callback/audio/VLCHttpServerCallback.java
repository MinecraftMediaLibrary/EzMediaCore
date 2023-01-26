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
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrameOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCStandardOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.sout.VLCTranscoderOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/*
 * VLCHttpServerCallback assumes that the output is already using a TCPOutput (meaning
 * VLC is directly sending the packets to the website), so all we have to do is to
 * prepare the HTTP server for audio.
 */
public final class VLCHttpServerCallback extends ServerCallback {

  VLCHttpServerCallback(
      @NotNull final MediaLibraryCore core, @NotNull final String host, final int port) {
    super(core, host, port);
  }

  @Override
  public void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    this.createStream(player, status);
  }

  private void createStream(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status) {
    if (status == PlayerControls.START || status == PlayerControls.RESUME) {
      final VLCMediaPlayer vlc = (VLCMediaPlayer) player;
      final VLCFrameOutput output = (VLCFrameOutput) vlc.getOutput();
      output.setTranscoder(this.getTranscoderOutput());
      output.setStandard(this.getStandardOutput());
    }
  }

  private @NotNull VLCTranscoderOutput getTranscoderOutput() {
    final VLCTranscoderOutput output = VLCTranscoderOutput.ofOutput();
    Map.of(
            VLCTranscoderOutput.VCODEC, "x264",
            VLCTranscoderOutput.ACODEC, "vorbis",
            VLCTranscoderOutput.SCODEC, "none")
        .forEach(output::setProperty);
    return output;
  }

  private @NotNull VLCStandardOutput getStandardOutput() {
    final VLCStandardOutput output = VLCStandardOutput.ofSection("http");
    final String host = this.getHost();
    final int port = this.getPort();
    Map.of(VLCStandardOutput.DST, "%s:%s/audio.ogg".formatted(host, port))
        .forEach(output::setProperty);
    return output;
  }

  @Override
  public void process(final byte @NotNull [] data) {}

  public static final class Builder extends ServerCallback.Builder {

    @Contract("_ -> new")
    @Override
    public @NotNull AudioOutput build(@NotNull final MediaLibraryCore core) {
      final String host = this.getHost();
      final int port = this.getPort();
      return new VLCHttpServerCallback(core, host, port);
    }
  }
}
