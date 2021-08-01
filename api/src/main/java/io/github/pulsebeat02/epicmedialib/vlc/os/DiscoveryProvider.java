package io.github.pulsebeat02.epicmedialib.vlc.os;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface DiscoveryProvider extends LibraryInjectable {

  @NotNull
  Optional<Path> discover(@NotNull final Path directory) throws IOException;

  @NotNull
  NativeDiscoveryAlgorithm getAlgorithm();

  @NotNull
  String getKeyword();

  boolean isHeuristics();
}
