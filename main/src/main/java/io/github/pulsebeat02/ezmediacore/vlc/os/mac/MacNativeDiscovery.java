package io.github.pulsebeat02.ezmediacore.vlc.os.mac;

import com.google.common.collect.ImmutableSet;
import com.sun.jna.NativeLibrary;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.NativeDiscoveryAlgorithm;
import java.nio.file.Path;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.RuntimeUtil;

public class MacNativeDiscovery implements NativeDiscoveryAlgorithm {
  @Override
  public @NotNull OSType getOperatingSystem() {
    return OSType.MAC;
  }

  @Override
  public @NotNull String getFileExtension() {
    return "dylib";
  }

  @Override
  public @NotNull Collection<String> getSearchPatterns() {
    return ImmutableSet.of("/lib/../plugins");
  }

  @Override
  public @NotNull void onLibVlcFound(@NotNull final Path discoveredPath) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), discoveredPath.toString());
    NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName());
  }
}
