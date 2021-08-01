package io.github.pulsebeat02.ezmediacore.callback;

import org.jetbrains.annotations.NotNull;

public interface ChatCallbackDispatcher extends Callback {

  @NotNull
  String getChatCharacter();
}
