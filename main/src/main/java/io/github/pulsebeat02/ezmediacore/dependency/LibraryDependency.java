package io.github.pulsebeat02.ezmediacore.dependency;

import io.github.pulsebeat02.ezmediacore.MediaLibraryCore;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public abstract sealed class LibraryDependency permits FFmpegDependencyManager,
    LibraryDependencyManager, SimpleRTSPServerDependencyManager, VLCDependencyManager {

  private final MediaLibraryCore core;

  LibraryDependency(@NotNull final MediaLibraryCore core) throws IOException {
    this.core = core;
  }

  public abstract void start() throws IOException;

  public abstract void onInstallation(@NotNull final Path path);

  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }
}
