package io.github.pulsebeat02.ezmediacore.capabilities;

import io.github.pulsebeat02.emcinstallers.implementation.rtsp.RTSPInstaller;

import java.io.IOException;
import java.nio.file.Path;

public final class RTPCapability extends Capability {

  private static Path RTP_BINARY_PATH;

  public RTPCapability() {
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
