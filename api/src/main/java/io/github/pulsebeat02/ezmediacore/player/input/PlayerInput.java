package io.github.pulsebeat02.ezmediacore.player.input;

import io.github.pulsebeat02.ezmediacore.player.input.Input;
import org.jetbrains.annotations.NotNull;

public interface PlayerInput {

  @NotNull
  Input getDirectVideoMrl();

  @NotNull
  Input getDirectAudioMrl();
}
