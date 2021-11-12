package io.github.pulsebeat02.ezmediacore.vlc;

import io.github.pulsebeat02.emcinstallers.implementation.vlc.VLCInstallationKit;
import io.github.pulsebeat02.ezmediacore.Logger;
import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import io.github.pulsebeat02.ezmediacore.locale.Locale;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class VLCDependency {

  private final MediaLibraryCore core;
  private final Path folder;

  public VLCDependency(@NotNull final MediaLibraryCore core) {
    this.core = core;
    this.folder = core.getVlcPath().getParent();
  }

  public void start() throws IOException {
    VLCInstallationKit.create(this.folder).start().ifPresent((path) -> {
      this.core.setVLCStatus(true);
      Logger.info(Locale.BINARY_PATHS.build("VLC", path));
    });
  }
}
