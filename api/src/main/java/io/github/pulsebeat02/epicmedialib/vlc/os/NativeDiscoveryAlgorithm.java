package io.github.pulsebeat02.epicmedialib.vlc.os;

import java.nio.file.Path;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface NativeDiscoveryAlgorithm extends HostOperator {

  @NotNull
  String getFileExtension();

  @NotNull
  Collection<String> getSearchPatterns();

  @NotNull
  void onLibVlcFound(@NotNull final Path discoveredPath);
}
