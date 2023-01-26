package io.github.pulsebeat02.ezmediacore.callback.audio;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.audio.AudioOutput;
import org.jetbrains.annotations.NotNull;

public abstract class DiscordCallback extends AudioOutput {

  DiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }
}
