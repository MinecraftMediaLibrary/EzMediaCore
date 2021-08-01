package io.github.pulsebeat02.ezmediacore.vlc.os.unix;

import com.google.common.collect.ImmutableList;
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
    return ImmutableList.of("/vlc/plugins", "/plugins");
  }

  @Override
  public @NotNull void onLibVlcFound(@NotNull final Path discoveredPath) {}
}
