package io.github.pulsebeat02.epicmedialib.callback;

import io.github.pulsebeat02.epicmedialib.dither.DitherAlgorithm;
import org.jetbrains.annotations.NotNull;

public interface MapCallbackDispatcher extends Callback {

  long getMapId();

  @NotNull
  DitherAlgorithm getAlgorithm();
}
