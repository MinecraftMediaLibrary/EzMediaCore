package io.github.pulsebeat02.ezmediacore.vlc.os;

import io.github.pulsebeat02.ezmediacore.LibraryInjectable;
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
