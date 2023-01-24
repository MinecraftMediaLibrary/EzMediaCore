package io.github.pulsebeat02.ezmediacore.player.output;

import org.jetbrains.annotations.NotNull;

public interface PlayerOutput {

  @NotNull
  Object getResultingOutput();

  void setOutput(@NotNull Object output);
}
