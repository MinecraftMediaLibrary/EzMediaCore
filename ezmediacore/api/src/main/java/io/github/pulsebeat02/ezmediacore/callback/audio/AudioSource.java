package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
import io.github.pulsebeat02.ezmediacore.callback.PlayerListener;
import org.jetbrains.annotations.NotNull;

public interface AudioSource extends LibraryInjectable, PlayerListener {

    void process(final byte @NotNull [] data);
}
