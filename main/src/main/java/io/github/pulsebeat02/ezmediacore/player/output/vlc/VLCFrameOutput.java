package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VLCFrameOutput implements VLCOutput<ConsumableOutput> {

  private ConsumableOutput output;

  private VLCTranscoderOutput transcoder;
  private VLCStandardOutput standard;

  VLCFrameOutput() {}

  @Contract(" -> new")
  public @NotNull VLCFrameOutput ofFrameOutput() {
    return new VLCFrameOutput();
  }

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
  public void setOutput(@NotNull final ConsumableOutput output) {
    this.output = output;
  }

  @Override
  public String toString() {
    return "--sout '#%s:%s'".formatted(this.transcoder, this.standard);
  }
}
