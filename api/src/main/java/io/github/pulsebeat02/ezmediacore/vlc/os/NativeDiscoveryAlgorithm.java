package io.github.pulsebeat02.ezmediacore.vlc.os;

import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface NativeDiscoveryAlgorithm extends HostOperator {

  @NotNull
  String getFileExtension();

  @NotNull
  List<String> getSearchPatterns();

  @NotNull
  void onLibVlcFound(@NotNull final Path discoveredPath);
}
