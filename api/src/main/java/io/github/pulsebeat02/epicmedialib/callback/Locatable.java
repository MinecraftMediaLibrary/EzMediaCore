package io.github.pulsebeat02.epicmedialib.callback;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface Locatable {

  @NotNull
  Location getLocation();
}
