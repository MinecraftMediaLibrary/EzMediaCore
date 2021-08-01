package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import io.github.pulsebeat02.epicmedialib.nms.PacketHandler;
import io.github.pulsebeat02.epicmedialib.utility.Dimension;
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
