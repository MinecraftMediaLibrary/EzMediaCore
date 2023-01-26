package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioSource;
import org.jetbrains.annotations.NotNull;

public abstract class AudioHandler implements AudioSource {

  private final MediaLibraryCore core;

  public AudioHandler(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
