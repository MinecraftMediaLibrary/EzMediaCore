package io.github.pulsebeat02.ezmediacore.vlc.os.unix;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.NativeDiscoveryAlgorithm;
import java.nio.file.Path;
import java.util.Collection;
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
  public @NotNull Collection<String> getSearchPatterns() {
    return ImmutableSet.of("/vlc/plugins", "/plugins");
  }

  @Override
  public @NotNull void onLibVlcFound(@NotNull final Path discoveredPath) {}
}
