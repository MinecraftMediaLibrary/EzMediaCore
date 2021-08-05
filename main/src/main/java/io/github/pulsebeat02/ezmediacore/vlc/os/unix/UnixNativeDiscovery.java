package io.github.pulsebeat02.ezmediacore.vlc.os.unix;

import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.NativeDiscoveryAlgorithm;
import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UnixNativeDiscovery implements NativeDiscoveryAlgorithm {

  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.UNIX;
  }

  @Override
  public @NotNull String getFileExtension() {
    return "so";
  }

  @Override
  public @NotNull List<String> getSearchPatterns() {
    return List.of("/vlc/plugins", "/plugins");
  }

  @Override
  public void onLibVlcFound(@NotNull final Path discoveredPath) {
  }
}
