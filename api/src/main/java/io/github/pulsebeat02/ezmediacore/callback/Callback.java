package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.nms.PacketHandler;
import io.github.pulsebeat02.ezmediacore.utility.Dimension;
import org.jetbrains.annotations.NotNull;

public interface Callback extends LibraryInjectable, Dimension, Viewable {

  void process(final int[] data);

  // width in pixels
  int getBlockWidth();

  int getFrameDelay();

  long getLastUpdated();

  void setLastUpdated(long lastUpdated);

  @NotNull
  PacketHandler getPacketHandler();
}
