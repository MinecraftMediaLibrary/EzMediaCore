package rewrite.capabilities;

import rewrite.dependency.NativePluginLoader;
import rewrite.installers.vlc.search.EnhancedNativeDiscovery;

import java.nio.file.Path;

public final class VLCCapability extends Capability {

  private static Path VLC_BINARY_PATH;

  public VLCCapability() {
    super(() -> {
      final EnhancedNativeDiscovery discovery = new EnhancedNativeDiscovery();
      if (discovery.discover()) {
        final NativePluginLoader loader = new NativePluginLoader();
        final String result = discovery.getDiscoveredPath();
        VLC_BINARY_PATH = Path.of(result);
        loader.executePhantomPlayers();
        return true;
      }
      return false;
    });
  }

  public Path getBinaryPath() {
    if (this.isDisabled()) {
      throw new IllegalStateException("VLC capability is not enabled!");
    }
    return VLC_BINARY_PATH;
  }
}
