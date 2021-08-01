package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.dither.DitherAlgorithm;
import org.jetbrains.annotations.NotNull;

public interface MapCallbackDispatcher extends Callback {

  long getMapId();

  @NotNull
  DitherAlgorithm getAlgorithm();
}
