package rewrite.capabilities;

import rewrite.installers.rtsp.RTSPInstaller;
import java.io.IOException;
import java.nio.file.Path;

public final class RTSPCapability extends Capability {

  private static Path RTSP_BINARY_PATH;

  public RTSPCapability() {
    super(() -> {
      try {
        final RTSPInstaller installer = RTSPInstaller.create();
        RTSP_BINARY_PATH = installer.download(true);
        return true;
      } catch (final IOException e) {
        return false;
      }
    });
  }

  public Path getBinaryPath() {
    if (this.isDisabled()) {
      throw new IllegalStateException("RTSP capability is not enabled!");
    }
    return RTSP_BINARY_PATH;
  }
}
