package io.github.pulsebeat02.ezmediacore.callback;

import io.github.pulsebeat02.ezmediacore.callback.entity.NamedStringCharacter;
import org.jetbrains.annotations.NotNull;

public interface CharacterBased {

  @NotNull
  NamedStringCharacter getStringName();
}
