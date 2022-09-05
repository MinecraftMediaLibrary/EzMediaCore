package io.github.pulsebeat02.ezmediacore.callback.rewrite.discord;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.rewrite.AudioHandler;
import org.jetbrains.annotations.NotNull;

public abstract class DiscordCallback extends AudioHandler {

  DiscordCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }
}
