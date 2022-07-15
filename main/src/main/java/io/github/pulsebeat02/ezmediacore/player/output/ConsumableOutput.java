package io.github.pulsebeat02.ezmediacore.player.output;

import io.github.pulsebeat02.ezmediacore.player.output.vlc.VLCFrame;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ConsumableOutput extends OutputHandler<VLCFrame> {

  void consume(@NotNull final VLCFrame frame);
}
