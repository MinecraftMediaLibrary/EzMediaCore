package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import io.github.pulsebeat02.ezmediacore.player.output.ConsumableOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public final class VLCConsumableOutput implements ConsumableOutput {

  private Consumer<Optional<int[]>> rgbSamples;
  private Consumer<Optional<byte[]>> audioSamples;

  public VLCConsumableOutput(@NotNull final Consumer<Optional<int[]>> rgbSamples, @NotNull final Consumer<Optional<byte[]>> audioSamples) {
    this.rgbSamples = rgbSamples;
    this.audioSamples = audioSamples;
  }
  @Override
  public @NotNull VLCFrame getRaw() {
    return null; // not used
  }

  @Override
  public void consume(@NotNull final VLCFrame frame) {
    this.rgbSamples.accept(frame.getRGBSamples());
    this.audioSamples.accept(frame.getAudioSamples());
  }

  @Override
  public void setVideoConsumer(@NotNull final Consumer<Optional<int[]>> consumer) {
    this.rgbSamples = consumer;
  }

  @Override
  public void setAudioConsumer(@NotNull final Consumer<Optional<byte[]>> consumer) {
    this.audioSamples = consumer;
  }

  @Override
  public @NotNull Consumer<Optional<int[]>> getVideoConsumer() {
    return this.rgbSamples;
  }

  @Override
  public @NotNull Consumer<Optional<byte[]>> getAudioConsumer() {
    return this.audioSamples;
  }
}
