package io.github.pulsebeat02.ezmediacore;

import io.github.pulsebeat02.ezmediacore.dependency.ArtifactInstaller;
import io.github.pulsebeat02.ezmediacore.dependency.FFmpegInstaller;
import io.github.pulsebeat02.ezmediacore.vlc.VLCBinaryLocator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;

public class DependencyLoader implements LibraryLoader {

  private final MediaLibraryCore core;

  public DependencyLoader(@NotNull final MediaLibraryCore core) {
    this.core = core;
  }

  @Override
  public @NotNull MediaLibraryCore getCore() {
    return this.core;
  }

  @Override
  public void start() throws ExecutionException, InterruptedException {
    CompletableFuture.allOf(
            CompletableFuture.runAsync(this::installFFmpeg),
            CompletableFuture.runAsync(this::installDependencies)
                .thenRunAsync(() -> new VLCBinaryLocator(this.core, this.core.getVlcPath())))
        .get();
  }

  private void installFFmpeg() {
    try {
      new FFmpegInstaller(this.core).start();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void installDependencies() {
    try {
      new ArtifactInstaller(this.core).start();
    } catch (final IOException
        | ReflectiveOperationException
        | URISyntaxException
        | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }
}
