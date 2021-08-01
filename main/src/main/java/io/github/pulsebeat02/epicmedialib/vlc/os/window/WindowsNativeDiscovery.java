package io.github.pulsebeat02.epicmedialib.vlc.os.window;

import com.google.common.collect.ImmutableSet;
import io.github.pulsebeat02.epicmedialib.analysis.OSType;
import io.github.pulsebeat02.epicmedialib.vlc.os.NativeDiscoveryAlgorithm;
import java.nio.file.Path;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class WindowsNativeDiscovery implements NativeDiscoveryAlgorithm {
  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.WINDOWS;
  }

  @Override
  public @NotNull String getFileExtension() {
    return "dll";
  }

  @Override
  public @NotNull Collection<String> getSearchPatterns() {
    return ImmutableSet.of("");
  }

  @Override
  public @NotNull void onLibVlcFound(@NotNull final Path discoveredPath) {}
}
