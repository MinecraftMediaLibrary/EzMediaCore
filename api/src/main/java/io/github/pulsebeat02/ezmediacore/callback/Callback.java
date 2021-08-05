package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.dimension.Dimensional;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.player.PlayerControls;
import org.jetbrains.annotations.NotNull;

public interface Callback extends LibraryInjectable, Dimensional, Viewable {

  void process(final int[] data);

  void preparePlayerStateChange(@NotNull final PlayerControls status);

  // width in pixels
  int getBlockWidth();

  int getFrameDelay();

  long getLastUpdated();

  void setLastUpdated(long lastUpdated);

  @NotNull
  PacketHandler getPacketHandler();
}
