package io.github.pulsebeat02.ezmediacore.persistent;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public abstract class PersistentObject<T> implements SerializableComponent<T> {

  private final Path path;

  public PersistentObject(@NotNull final Path path) {
    this.path = path;
  }

  @Override
  public @NotNull Path getStorageFile() {
    return this.path;
  }
}
