package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import org.jetbrains.annotations.NotNull;

public abstract class AudioOutput implements AudioSource {

  private final MediaLibraryCore core;

  public AudioOutput(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
