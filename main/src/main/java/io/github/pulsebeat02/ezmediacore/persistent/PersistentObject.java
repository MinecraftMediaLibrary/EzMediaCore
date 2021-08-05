package io.github.pulsebeat02.ezmediacore.persistent;

import io.github.pulsebeat02.ezmediacore.utility.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

  @Override
  public void serialize(@NotNull final Collection<T> list) throws IOException {
    this.createFile();
  }

  @Override
  public List<T> deserialize() throws IOException {
    this.createFile();
    return Collections.emptyList();
  }

  private void createFile() throws IOException {
    final Path path = this.getStorageFile();
    FileUtils.createIfNotExists(path);
  }
}
