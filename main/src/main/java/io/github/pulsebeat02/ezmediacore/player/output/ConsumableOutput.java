package io.github.pulsebeat02.ezmediacore.player.output;

import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrame;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public interface ConsumableOutput extends OutputHandler<VLCFrame> {

  void consume(@NotNull final VLCFrame frame);

  void setVideoConsumer(@NotNull final Consumer<Optional<int[]>> consumer);

  void setAudioConsumer(@NotNull final Consumer<Optional<byte[]>> consumer);

  @NotNull Consumer<Optional<int[]>> getVideoConsumer();

  @NotNull Consumer<Optional<byte[]>> getAudioConsumer();
}
