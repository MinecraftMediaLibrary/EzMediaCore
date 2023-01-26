package io.github.pulsebeat02.ezmediacore.callback.audio;

import org.jetbrains.annotations.NotNull;

public interface ServerCallbackBase {
  @NotNull
  String getHost();

  int getPort();
}
