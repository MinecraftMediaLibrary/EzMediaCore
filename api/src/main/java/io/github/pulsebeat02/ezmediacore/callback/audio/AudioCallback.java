package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.callback.PlayerListener;
import io.github.pulsebeat02.ezmediacore.callback.Viewable;
import org.jetbrains.annotations.NotNull;

public interface AudioCallback extends LibraryInjectable, Viewable, PlayerListener {

  void process(final byte @NotNull [] data);
}
