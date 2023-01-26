package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.NotNull;

public interface ServerCallbackProxy {
  @NotNull
  String getHost();

  int getPort();
}
