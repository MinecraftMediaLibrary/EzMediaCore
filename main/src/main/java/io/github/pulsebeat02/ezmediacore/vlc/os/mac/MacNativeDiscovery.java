package io.github.pulsebeat02.ezmediacore.vlc.os.mac;

import com.sun.jna.NativeLibrary;
import io.github.pulsebeat02.ezmediacore.analysis.OSType;
import io.github.pulsebeat02.ezmediacore.vlc.os.NativeDiscoveryAlgorithm;
import java.nio.file.Path;
import java.util.List;
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
  public @NotNull List<String> getSearchPatterns() {
    return List.of("/lib/../plugins");
  }

  @Override
  public void onLibVlcFound(@NotNull final Path discoveredPath) {
    NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcCoreLibraryName(), discoveredPath.toString());
    NativeLibrary.getInstance(RuntimeUtil.getLibVlcCoreLibraryName());
  }
}
