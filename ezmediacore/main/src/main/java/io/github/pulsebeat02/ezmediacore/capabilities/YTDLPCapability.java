package io.github.pulsebeat02.ezmediacore.capabilities;

import io.github.pulsebeat02.ezmediacore.installers.ytdlp.YTDLPInstaller;

import java.io.IOException;
import java.nio.file.Path;

public final class YTDLPCapability extends Capability {

  private static Path YT_DLP_BINARY_PATH;

  public YTDLPCapability() {
    super(() -> {
      try {
        final YTDLPInstaller installer = YTDLPInstaller.create();
        YT_DLP_BINARY_PATH = installer.download(true);
        return true;
      } catch (final IOException e) {
        return false;
      }
    });
  }

  public Path getBinaryPath() {
    if (this.isDisabled()) {
      throw new IllegalStateException("YT-DLP capability is not enabled!");
    }
    return YT_DLP_BINARY_PATH;
  }
}
