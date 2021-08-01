package io.github.pulsebeat02.epicmedialib.vlc;

import io.github.pulsebeat02.epicmedialib.LibraryInjectable;
import io.github.pulsebeat02.epicmedialib.vlc.os.DiscoveryProvider;
import io.github.pulsebeat02.epicmedialib.vlc.os.WellKnownDirectoryProvider;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface BinarySearcher extends LibraryInjectable {

  @NotNull
  DiscoveryProvider getDiscoveryMethod();

  @NotNull
  Path getSearchPath();

  @NotNull
  Path getVlcPath();

  @NotNull
  Collection<WellKnownDirectoryProvider> getWellKnownDirectories();

  Optional<Path> search() throws IOException;

  boolean addDiscoveryDirectory(@NotNull final WellKnownDirectoryProvider provider);
}
