package io.github.pulsebeat02.ezmediacore.player.output.vlc;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface VLCFrame {

  @NotNull
  Optional<int[]> getRGBSamples();

  @NotNull
  Optional<byte[]> getAudioSamples();
}
