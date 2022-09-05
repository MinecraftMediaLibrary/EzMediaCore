package io.github.pulsebeat02.ezmediacore.callback.rewrite.http;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.callback.rewrite.AudioHandler;
import org.jetbrains.annotations.NotNull;

public abstract class ServerCallback extends AudioHandler {

  public ServerCallback(@NotNull final MediaLibraryCore core) {
    super(core);
  }
}
