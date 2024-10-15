package io.github.pulsebeat02.ezmediacore.pipeline.input;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class PathInput implements Input {

  private final Path path;

  public PathInput(final Path path) {
    this.path = path;
  }

  @Override
  public CompletableFuture<String> getMediaRepresentation() {
    final Path absolute = this.path.toAbsolutePath();
    final String raw = absolute.toString();
    return CompletableFuture.completedFuture(raw);
  }

  public Path getPath() {
    return this.path;
  }
}
