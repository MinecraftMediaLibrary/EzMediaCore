package io.github.pulsebeat02.epicmedialib;

import org.jetbrains.annotations.NotNull;

public interface LibraryInjectable {

  @NotNull
  MediaLibraryCore getCore();
}
