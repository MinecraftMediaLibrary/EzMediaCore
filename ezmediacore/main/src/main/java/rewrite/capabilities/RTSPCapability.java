package rewrite.capabilities;

import rewrite.installers.RTSPInstaller;
import java.io.IOException;
import java.nio.file.Path;

public final class RTSPCapability extends Capability {

  private static Path RTP_BINARY_PATH;

  public RTSPCapability() {
    super(() -> {
      try {
        final RTSPInstaller installer = RTSPInstaller.create();
        RTP_BINARY_PATH = installer.download(true);
        return true;
      } catch (final IOException e) {
        return false;
      }
    });
  }

  public Path getBinaryPath() {
    return RTP_BINARY_PATH;
  }
}
