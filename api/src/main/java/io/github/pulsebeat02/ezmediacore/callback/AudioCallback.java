package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import org.jetbrains.annotations.NotNull;

public interface AudioCallback extends LibraryInjectable, Viewable, PlayerListener {

  void process(final byte @NotNull [] data);
}
