package io.github.pulsebeat02.epicmedialib.callback;

import org.jetbrains.annotations.NotNull;

public interface ChatCallbackDispatcher extends Callback {

  @NotNull
  String getChatCharacter();
}
