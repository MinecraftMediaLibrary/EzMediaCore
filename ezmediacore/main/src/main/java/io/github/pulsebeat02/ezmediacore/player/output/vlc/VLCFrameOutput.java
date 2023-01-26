package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import com.google.common.base.Preconditions;
import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;
import io.github.pulsebeat02.ezmediacore.player.output.vlc.sout.VLCTranscoderOutput;
import org.jetbrains.annotations.NotNull;

public final class VLCFrameOutput implements VLCOutput {

  private ConsumableOutput output;

  private VLCTranscoderOutput transcoder;
  private VLCStandardOutput standard;

  public VLCFrameOutput() {}

  public void setTranscoder(@NotNull final VLCTranscoderOutput transcoder) {
    this.transcoder = transcoder;
  }

  public void setStandard(@NotNull final VLCStandardOutput standard) {
    this.standard = standard;
  }

  @Override
  public @NotNull ConsumableOutput getResultingOutput() {
    return this.output;
  }

  @Override
  public void setOutput(@NotNull final Object output) {
    Preconditions.checkArgument(output instanceof ConsumableOutput);
    this.output = (ConsumableOutput) output;
  }

  @Override
  public String toString() {
    return ":sout=#%s:%s".formatted(this.transcoder, this.standard);
  }
}
