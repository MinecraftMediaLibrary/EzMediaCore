package io.github.pulsebeat02.ezmediacore.callback;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Viewable {

  @NotNull
  UUID[] getViewers();
}
