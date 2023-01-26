package io.github.pulsebeat02.ezmediacore.callback.audio.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import io.github.pulsebeat02.ezmediacore.player.external.VLCMediaPlayer;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrameOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCStandardOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.sout.VLCTranscoderOutput;
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
    final VLCTranscoderOutput output = new VLCTranscoderOutput();
    Map.of(
            VLCTranscoderOutput.VCODEC, "x264",
            VLCTranscoderOutput.ACODEC, "vorbis",
            VLCTranscoderOutput.SCODEC, "none")
        .forEach(output::setProperty);
    return output;
  }

  private @NotNull VLCStandardOutput getStandardOutput() {
    final VLCStandardOutput output = new VLCStandardOutput("http");
    final String host = this.getHost();
    final int port = this.getPort();
    Map.of(VLCStandardOutput.DST, "%s:%s/audio.ogg".formatted(host, port))
        .forEach(output::setProperty);
    return output;
  }

  @Override
  public void process(final byte @NotNull [] data) {}
}
