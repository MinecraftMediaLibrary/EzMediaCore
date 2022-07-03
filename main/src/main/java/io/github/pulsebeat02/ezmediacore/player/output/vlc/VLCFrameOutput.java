package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.ConfiguredOutput;
import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VLCFrameOutput implements VLCOutput<ConsumableOutput> {

  private final ConsumableOutput output;

  private ConfiguredOutput transcoder;
  private ConfiguredOutput standard;

  VLCFrameOutput(@NotNull final ConsumableOutput output) {
    this.output = output;
  }

  @Contract("_ -> new")
  public @NotNull VLCFrameOutput ofFrameOutput(@NotNull final ConsumableOutput output) {
    return new VLCFrameOutput(output);
  }

  public @NotNull ConfiguredOutput getTranscoder() {
    return this.transcoder;
  }

  public @NotNull ConfiguredOutput getStandard() {
    return this.standard;
  }

  public void setTranscoder(@NotNull final ConfiguredOutput transcoder) {
    this.transcoder = transcoder;
  }

  public void setStandard(@NotNull final ConfiguredOutput standard) {
    this.standard = standard;
  }

  @Override
  public @NotNull ConsumableOutput getResultingOutput() {
    return this.output;
  }
}
