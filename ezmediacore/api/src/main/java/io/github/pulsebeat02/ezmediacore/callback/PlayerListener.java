package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import io.github.pulsebeat02.ezmediacore.player.VideoPlayer;
import org.jetbrains.annotations.NotNull;

public interface PlayerListener {

  void preparePlayerStateChange(
      @NotNull final VideoPlayer player, @NotNull final PlayerControls status);
}
